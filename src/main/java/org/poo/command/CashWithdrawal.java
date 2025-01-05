package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.OutputGenerator;

public class CashWithdrawal implements Command {

    private String cardNumber;
    private String userEmail;
    private double amount;
    private String location;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public CashWithdrawal(CommandInput command) {
        cardNumber = command.getCardNumber();
        userEmail = command.getEmail();
        amount = command.getAmount();
        location = command.getLocation();
        timestamp = command.getTimestamp();
    }

    public void checkAmount(final User user, final Account acc, final Card card) {

        double commSum = acc.getPlan().getPlanStrategy().
                        commissionStrategy(amount, acc.getCurrency());

        if (acc.getCurrency().equals("RON")) {

            if (acc.canPay(commSum)) {
                acc.decrementFunds(commSum);
                return;
            }

        } else {

            double exchangeRate = ExchangeRateDatabase.getInstance().
                                    getExchangeRate(acc.getCurrency(), "RON");

            double convAmount = commSum / exchangeRate;

            if (acc.canPay(convAmount)) {
                acc.decrementFunds(convAmount);
                return;
            }
        }
        actionCode = CommandConstants.INSUFFICIENT_FUNDS;

    }

    public void checkCardStatus(final User user, final Account acc, final Card card) {

        if (card.getStatus().toString().equals("frozen")) {
            actionCode = CommandConstants.FROZEN_CARD;
            return;
        }

        checkAmount(user, acc, card);

    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        User user = userDatabase.getUserEntry(userEmail);

        if (user == null) {
            // NOT_FOUND in this context means the user wasn't found
            actionCode = CommandConstants.NOT_FOUND;
            return;
        }

        for (Account acc : user.getUserAccounts().values()) {
            if (acc.getCards().containsKey(cardNumber)) {
                checkCardStatus(user, acc, acc.getCards().get(cardNumber));
                return;
            }

//            if (acc.getDeletedOneTimeCards().contains(cardNumber)) {
//                actionCode = CommandConstants.DELETED_CARD;
//                return;
//            }
        }

        actionCode = CommandConstants.UNKNOWN_CARD;

    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        ObjectNode transactionNode = outputGenerator.getMapper().createObjectNode();

        switch (actionCode) {

            case SUCCESS -> {
                transactionNode.put("timestamp", timestamp);
                transactionNode.put("description", "Cash withdrawal of " + amount);
                transactionNode.put("amount", amount);
                UserDatabase.getInstance().getUserEntry(userEmail).addTransaction(transactionNode);
                break;
            }

            case UNKNOWN_CARD -> {
                outputGenerator.errorSetting(timestamp, "Card not found", "cashWithdrawal");
                break;
            }

            case INSUFFICIENT_FUNDS -> {
                transactionNode.put("timestamp", timestamp);
                transactionNode.put("description", "Insufficient Funds");
                UserDatabase.getInstance().getUserEntry(userEmail).addTransaction(transactionNode);
                break;
            }

            case DELETED_CARD -> {
                transactionNode.put("timestamp", timestamp);
                transactionNode.put("description", "Card has already been used");
                break;
            }

            case FROZEN_CARD -> {
                transactionNode.put("timestamp", timestamp);
                transactionNode.put("description", "Card is frozen");
                break;
            }

            default -> {
                break;
            }
        }

    }
}
