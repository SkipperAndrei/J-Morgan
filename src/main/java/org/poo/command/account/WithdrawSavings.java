package org.poo.command.account;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.SavingAccount;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.OutputGenerator;


public final class WithdrawSavings implements Command {

    private String account;
    private String receiverAccount;
    private double amount;
    private String currency;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public WithdrawSavings(final CommandInput command) {
        account = command.getAccount();
        amount = command.getAmount();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
    }

    /**
     * This function will update the balances in the accounts based on the service plan
     * @param savingAcc The saving account of the user
     * @param classicAcc The classic account of the user
     * @param deductedAmm The amount to be deducted from the savings account
     */
    public void handlePayment(final Account savingAcc,
                              final Account classicAcc, final double deductedAmm) {

        savingAcc.decrementFunds(deductedAmm);
        receiverAccount = classicAcc.getIban();
        classicAcc.incrementFunds(amount);
    }

    /**
     * This function checks if the savings account has enough funds to do the withdrawal
     * If it does, it will generate a Success code
     * If it doesn't, it will generate an INSUFFICIENT_FUNDS error signal
     * @param user The user that requested the command
     * @param classicAcc The account where the money will be sent
     */
    public void checkFundsAccount(final User user, final Account classicAcc) {

        Account userSavingAccount = user.getUserAccounts().get(account);
        double userSavingBalance = userSavingAccount.getBalance();

        if (userSavingAccount.getCurrency().equals(currency)) {

            if (userSavingBalance < amount) {

                actionCode = CommandConstants.INSUFFICIENT_FUNDS;
                return;
            }

            handlePayment(userSavingAccount, classicAcc, amount);
        } else {

            double rate = ExchangeRateDatabase.getInstance().
                            getExchangeRate(currency, classicAcc.getCurrency());

            if (userSavingBalance < amount * rate) {

                actionCode = CommandConstants.INSUFFICIENT_FUNDS;
                return;
            }

            handlePayment(userSavingAccount, classicAcc, amount * rate);
        }

        actionCode = CommandConstants.SUCCESS;
    }

    /**
     * This function checks if the user has a classic account to transfer the savings there
     * The search is done with the account type and currency as the main criteria
     * An error signal will be returned if the search failed
     * @param user The user that requested the command
     */
    public void checkClassicAccount(final User user) {

        for (Account acc : user.getUserAccounts().values()) {

            if (acc.getAccountType().equals("classic") && acc.getCurrency().equals(currency)) {
                checkFundsAccount(user, acc);
                return;
            }
        }

        actionCode = CommandConstants.UNKNOWN_CURRENCY;
    }

    /**
     * This method will check if the account from where the user wants to withdraw
     * is a savings account
     * @param user The user that requested the command
     */
    public void checkSavingsAccount(final User user) {

        Account acc = user.getUserAccounts().get(account);

        try {
            ((SavingAccount) acc).getInterestRate();
            checkClassicAccount(user);
        } catch (ClassCastException e) {
            actionCode = CommandConstants.CLASSIC_ACC;
        }
    }

    /**
     * This function will handle user age checking
     * User must be 21 years or older in order for the verifications to proceed
     * Otherwise, an error signal will be returned
     * @param user The user that requested the command
     */
    public void checkUserAge(final User user) {

        LocalDate userBirthday = LocalDate.parse(user.getUserData().getBirthDate(),
                                                DateTimeFormatter.ISO_DATE);

        LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        long userYears = ChronoUnit.YEARS.between(userBirthday, currentDate);

        if (userYears >= CommandConstants.ADULT_AGE.getValue()) {

            checkSavingsAccount(user);
        } else {

            actionCode = CommandConstants.MINIMUM_AGE;
        }

    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        try {

            String userEmail = userDatabase.getMailEntry(account);
            User user = userDatabase.getUserEntry(userEmail);
            checkUserAge(user);
        } catch (NullPointerException e) {

            actionCode = CommandConstants.NOT_FOUND;
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        ObjectNode transactionNode = outputGenerator.getMapper().createObjectNode();
        String userEmail = outputGenerator.getUserDatabase().getMailEntry(account);
        User user = outputGenerator.getUserDatabase().getUserEntry(userEmail);
        Account acc = user.getUserAccounts().get(account);
        Account classicAcc = user.getUserAccounts().get(receiverAccount);

        switch (actionCode) {

            case SUCCESS:

                transactionNode.put("amount", amount);
                transactionNode.put("classicAccountIBAN", receiverAccount);
                transactionNode.put("description", "Savings withdrawal");
                transactionNode.put("savingsAccountIBAN", account);
                transactionNode.put("timestamp", timestamp);

                user.addTransaction(transactionNode);
                user.addTransaction(transactionNode);
                acc.addTransaction(transactionNode);
                classicAcc.addTransaction(transactionNode);
                break;


            case MINIMUM_AGE:

                transactionNode.put("description", "You don't have the minimum age required.");
                transactionNode.put("timestamp", timestamp);
                user.addTransaction(transactionNode);
                acc.addTransaction(transactionNode);
                break;

            case UNKNOWN_CURRENCY:

                transactionNode.put("description", "You do not have a classic account.");
                transactionNode.put("timestamp", timestamp);
                user.addTransaction(transactionNode);
                acc.addTransaction(transactionNode);
                break;

            default :
                return;
        }
    }
}
