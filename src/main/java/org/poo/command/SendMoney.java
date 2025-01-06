package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.database.CommerciantDatabase;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;
import org.poo.user.User;

public final class SendMoney implements Command {

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
    private CommandConstants actionCode;
    private CommandConstants receiverCode = CommandConstants.USER_REC;

    public SendMoney(final CommandInput command, final ExchangeRateDatabase exchangeRateDatabase) {

        email = command.getEmail();
        account = command.getAccount();
        receiver = command.getReceiver();
        amount = command.getAmount();
        originalAmount = amount;
        description = command.getDescription();
        timestamp = command.getTimestamp();
        this.exchangeRateDatabase = exchangeRateDatabase;
    }

    /**
     * This function checks if the sender has enough funds to pay
     * If the sender has enough the payment will happen
     * If not, an error code will be signaled
     * @param senderAcc The sender account
     * @param receiverAcc The receiver account
     */
    public void executeOrError(final Account senderAcc, final Account receiverAcc) {

        double commSum = senderAcc.getPlan().getPlanStrategy().
                        commissionStrategy(originalAmount, senderCurrency);

        if (senderAcc.getBalance() < commSum) {
            actionCode = CommandConstants.INSUFFICIENT_FUNDS;
            return;
        }

        senderAcc.decrementFunds(commSum);
        senderAcc.setBalance(Math.round(senderAcc.getBalance() * 100.0) / 100.0);
        // System.out.println("Dupa plata de la timestamp " + timestamp + " user-ul " + email + " mai are " + senderAcc.getBalance());

        // TODO: Implement auto-upgrade logic if necessary

        if (receiverCode.equals(CommandConstants.USER_REC)) {
            receiverAcc.incrementFunds(amount);
            // System.out.println("Dupa primirea de la timestamp " + timestamp + " user-ul " + receiverAcc.getEmail() + " are " + receiverAcc.getBalance());

        } else {
            // System.out.println("Timestamp " + timestamp);
            senderAcc.handleCommerciantPayment(receiver, amount);
        }

        actionCode = CommandConstants.SUCCESS;
    }

    /**
     * This function will convert the amount to be paid to the sender account's currency
     * @param senderAcc The sender account
     * @param receiverAcc The receiver account necessary for the execution of the command
     */
    public void checkAmount(final Account senderAcc, final Account receiverAcc) {

        if (senderAcc.getCurrency().equals(receiverAcc.getCurrency())) {
            executeOrError(senderAcc, receiverAcc);
            return;
        }

        amount *= exchangeRateDatabase.getExchangeRate(senderAcc.getCurrency(),
                                                        receiverAcc.getCurrency());

        executeOrError(senderAcc, receiverAcc);

    }

    /**
     * This function will check if the receiver account of the "Send Money" command is valid
     * @param userDatabase The database that will be queried
     * @param senderAcc The sender account
     */
    public void checkReceiver(final UserDatabase userDatabase, final Account senderAcc) {

        for (User user : userDatabase.getDatabase().values()) {

            if (user.getUserAccounts().containsKey(receiver)) {
                checkAmount(senderAcc, user.getUserAccounts().get(receiver));
                return;
            }

            if (user.getUserAliasAccounts().containsKey(receiver)) {
                checkAmount(senderAcc, user.getUserAliasAccounts().get(receiver));
                return;
            }
        }
        actionCode = CommandConstants.NOT_FOUND;

    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {


        if (userDatabase.getUserEntry(email).getUserAccounts().containsKey(account)) {
            senderCurrency = userDatabase.getUserEntry(email).getUserAccounts().
                            get(account).getCurrency();

            Integer commId = CommerciantDatabase.getInstance().getCommIbanToId().get(receiver);

            try {
                if (commId != null && CommerciantDatabase.getInstance().getCommerciantDb().containsKey(commId)) {

                    Account send = userDatabase.getUserEntry(email).getUserAccounts().get(account);
                    receiverCode = CommandConstants.COMMERCIANT_REC;
                    executeOrError(send, null);
                    return;
                }

                throw new NullPointerException();

            } catch (NullPointerException e) {
                checkReceiver(userDatabase,
                        userDatabase.getDatabase().get(email).getUserAccounts().get(account));
            }

        }


    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        switch (actionCode) {
            case SUCCESS:
                ObjectNode sendNode = outputGenerator.getMapper().createObjectNode();

                sendNode.put("timestamp", timestamp);
                sendNode.put("description", description);
                sendNode.put("senderIBAN", account);
                sendNode.put("receiverIBAN", receiver);
                sendNode.put("amount", originalAmount + " " + senderCurrency);
                sendNode.put("transferType", "sent");

                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(sendNode);


                Account sentAcc = outputGenerator.getUserDatabase().
                                getUserEntry(email).getUserAccounts().get(account);

                outputGenerator.tryToAddTransaction(sentAcc, sendNode);

                if (receiverCode.equals(CommandConstants.USER_REC)) {

                    ObjectNode receivedMoneyNode = sendNode.deepCopy();
                    receivedMoneyNode.put("transferType", "received");

                    emailReceiver = outputGenerator.getUserDatabase().getMailEntry(receiver);
                    Account receivedAcc = outputGenerator.getUserDatabase().
                            getUserEntry(emailReceiver).getUserAccounts().get(receiver);

                    receivedMoneyNode.put("amount", amount + " " + receivedAcc.getCurrency());

                    outputGenerator.getUserDatabase().getUserEntry(emailReceiver).
                            addTransaction(receivedMoneyNode);

                    outputGenerator.tryToAddTransaction(receivedAcc, receivedMoneyNode);

                }
                return;

            case INSUFFICIENT_FUNDS:
                ObjectNode noFundsNode = outputGenerator.getMapper().createObjectNode();
                noFundsNode.put("timestamp", timestamp);
                noFundsNode.put("description", "Insufficient funds");
                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(noFundsNode);

                Account insufficientAcc = outputGenerator.getUserDatabase().
                                            getUserEntry(email).getUserAccounts().get(account);

                outputGenerator.tryToAddTransaction(insufficientAcc, noFundsNode);
                return;

            case NOT_FOUND:
                outputGenerator.errorSetting(timestamp, "User not found", "sendMoney");
                return;

            default :
                return;
        }
    }
}
