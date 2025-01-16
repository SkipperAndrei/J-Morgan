package org.poo.command.account;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.OutputGenerator;

public final class CashWithdrawal implements Command {

    private String cardNumber;
    private String userEmail;
    private double amount;
    private String location;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public CashWithdrawal(final CommandInput command) {
        cardNumber = command.getCardNumber();
        userEmail = command.getEmail();
        amount = command.getAmount();
        location = command.getLocation();
        timestamp = command.getTimestamp();
    }

    /**
     * This method checks if the user has enough funds to withdraw the funds requested
     * The amount is first converted into RON's, because the ATM only calculates in RON's
     * @param user The user that requested the query
     * @param acc The account from where the query was requested
     * @param card The card inserted into the ATM
     */
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

    /**
     * This function checks if the card is frozen
     * @param user The user that requested the query
     * @param acc The account that the card is associated to
     * @param card The card inserted into the ATM
     */
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

            actionCode = CommandConstants.NOT_FOUND;
            return;
        }

        for (Account acc : user.getUserAccounts().values()) {
            if (acc.getCards().containsKey(cardNumber)) {
                checkCardStatus(user, acc, acc.getCards().get(cardNumber));
                return;
            }

        }

        actionCode = CommandConstants.UNKNOWN_CARD;

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

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
                transactionNode.put("description", "Insufficient funds");
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

            case NOT_FOUND -> {
                outputGenerator.errorSetting(timestamp, "User not found", "cashWithdrawal");
                break;
            }

            default -> {
                break;
            }
        }

    }
}
