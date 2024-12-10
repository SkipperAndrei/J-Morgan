package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.poo.account.Account;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;
import org.poo.user.User;

public class SendMoney implements Command {

    private final static int WRONG_OWNER = -1;
    private final static int NON_EXISTENT_ACC = -2;
    private final static int INSUFFICIENT_FUNDS = -3;
    private final static int SAVINGS_ACC = -4;
    private final static int SUCCESS = 0;

    private String email;
    private String emailReceiver;
    private String account;
    private String receiver;
    private double originalAmount;
    private double amount;
    private String description;
    private int timestamp;
    private String senderCurrency;
    private ExchangeRateDatabase exchangeRateDatabase;
    private int actionCode = WRONG_OWNER;

    public SendMoney(CommandInput command, ExchangeRateDatabase exchangeRateDatabase) {

        email = command.getEmail();
        account = command.getAccount();
        receiver = command.getReceiver();
        amount = command.getAmount();
        originalAmount = amount;
        description = command.getDescription();
        timestamp = command.getTimestamp();
        this.exchangeRateDatabase = exchangeRateDatabase;
    }

    public void executeOrError(final Account senderAcc, final Account receiverAcc) {
        if (senderAcc.getBalance() < originalAmount) {
            actionCode = INSUFFICIENT_FUNDS;
            return;
        }
        senderAcc.decrementFunds(originalAmount);
        receiverAcc.incrementFunds(amount);
        actionCode = SUCCESS;
    }

    public void checkAmount(final Account senderAcc, final Account receiverAcc) {

        if (senderAcc.getCurrency().equals(receiverAcc.getCurrency())) {
            executeOrError(senderAcc, receiverAcc);
            return;
        }

        amount *= exchangeRateDatabase.getExchangeRate(senderAcc.getCurrency(), receiverAcc.getCurrency());
        executeOrError(senderAcc, receiverAcc);

    }

    public void checkReceiver(UserDatabase userDatabase, Account senderAcc) {

        for (User user : userDatabase.getDatabase().values()) {

            if (user.getUserAccounts().containsKey(receiver)) {
                checkAmount(senderAcc, user.getUserAccounts().get(receiver));
                return;
            }

            if (user.getUserAliasAccounts().containsKey(receiver)) {
                checkAmount(senderAcc, user.getUserAliasAccounts().get(receiver));
            }
        }
        actionCode = NON_EXISTENT_ACC;

    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        if (userDatabase.getUserEntry(email).getUserAccounts().containsKey(account)) {
            senderCurrency = userDatabase.getUserEntry(email).getUserAccounts().get(account).getCurrency();
            checkReceiver(userDatabase,
                          userDatabase.getDatabase().get(email).getUserAccounts().get(account));
            return;
        }

    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        switch (actionCode) {
            case SUCCESS:
                ObjectNode sendMoneyNode = outputGenerator.getMapper().createObjectNode();

                sendMoneyNode.put("timestamp", timestamp);
                sendMoneyNode.put("description", description);
                sendMoneyNode.put("senderIBAN", account);
                sendMoneyNode.put("receiverIBAN", receiver);
                sendMoneyNode.put("amount", originalAmount + " " + senderCurrency);
                sendMoneyNode.put("transferType", "sent");

                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(sendMoneyNode);

                ObjectNode receivedMoneyNode = sendMoneyNode.deepCopy();
                Account sentAcc = outputGenerator.getUserDatabase().
                                getUserEntry(email).getUserAccounts().get(account);

                outputGenerator.tryToAddTransaction(sentAcc, sendMoneyNode);
                receivedMoneyNode.put("transferType", "received");

                emailReceiver = outputGenerator.getUserDatabase().getMailEntry(receiver);
                Account receivedAcc = outputGenerator.getUserDatabase().
                                    getUserEntry(emailReceiver).getUserAccounts().get(receiver);
                receivedMoneyNode.put("amount", amount + " " + receivedAcc.getCurrency());

                outputGenerator.getUserDatabase().getUserEntry(emailReceiver).addTransaction(receivedMoneyNode);
                outputGenerator.tryToAddTransaction(receivedAcc, receivedMoneyNode);
                return;

            case INSUFFICIENT_FUNDS:
                ObjectNode noFundsNode = outputGenerator.getMapper().createObjectNode();
                noFundsNode.put("timestamp", timestamp);
                noFundsNode.put("description", "Insufficient funds");
                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(noFundsNode);

                Account insufficientAcc = outputGenerator.getUserDatabase().getUserEntry(email).getUserAccounts().get(account);
                outputGenerator.tryToAddTransaction(insufficientAcc, noFundsNode);
                return;

            default :
                return;
        }
    }
}
