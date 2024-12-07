package org.poo.command;

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
    private final static int SUCCESS = 0;

    private String email;
    private String account;
    private String receiver;
    private double originalAmount;
    private double amount;
    private String description;
    private int timestamp;
    private ExchangeRateDatabase exchangeRateDatabase;
    private int actionCode = WRONG_OWNER;

    public SendMoney(CommandInput command, ExchangeRateDatabase exchangeRateDatabase) {

        email = command.getEmail();
        account = command.getAccount();
        receiver = command.getReceiver();
        amount = command.getAmount();
        description = command.getDescription();
        timestamp = command.getTimestamp();
        this.exchangeRateDatabase = exchangeRateDatabase;
    }

    public void executeOrError(final Account senderAcc, final Account receiverAcc) {
        if (senderAcc.getBalance() < amount) {
            actionCode = INSUFFICIENT_FUNDS;
            return;
        }
        senderAcc.decrementFunds(amount);
        receiverAcc.incrementFunds(amount);
        actionCode = SUCCESS;
    }

    public void checkAmount(final Account senderAcc, final Account receiverAcc) {

        if (senderAcc.getCurrency().equals(receiverAcc.getCurrency())) {
            originalAmount = amount;
            executeOrError(senderAcc, receiverAcc);
            return;
        }

        if (exchangeRateDatabase.addUnknownExchange(receiverAcc.getCurrency(), senderAcc.getCurrency())) {
            DefaultWeightedEdge edge = exchangeRateDatabase.getExchangeGraph().
                                        getEdge(receiverAcc.getCurrency(), senderAcc.getCurrency());
            originalAmount = amount;
            amount *= exchangeRateDatabase.getExchangeGraph().getEdgeWeight(edge);
            executeOrError(senderAcc, receiverAcc);
        }
    }

    public void checkReceiver(UserDatabase userDatabase, Account senderAcc) {

        for (User user : userDatabase.getDatabase().values()) {

            if (user.getUserAccounts().containsKey(receiver)) {
                checkAmount(senderAcc, user.getUserAccounts().get(receiver));
                return;
            }
        }
        actionCode = NON_EXISTENT_ACC;

    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        if (userDatabase.getDatabase().get(email).getUserAccounts().containsKey(account)) {
            checkReceiver(userDatabase,
                          userDatabase.getDatabase().get(email).getUserAccounts().get(account));
        }


    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        return;
    }
}
