package org.poo.command;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.SavingAccount;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.OutputGenerator;


public final class WithdrawSavings implements Command {

    private String account;
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
     * @param user The user that requested the command
     * @param savingAcc The saving account of the user
     * @param classicAcc The classic account of the user
     * @param amount The amount to be put in the classic account
     */
    public void handlePayment(final User user, final Account savingAcc,
                              final Account classicAcc) {

        double newAmm = savingAcc.getPlan().getPlanStrategy().commissionStrategy(amount, currency);
        savingAcc.decrementFunds(amount);
        classicAcc.incrementFunds(newAmm);
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

            handlePayment(user, userSavingAccount, classicAcc);
            actionCode = CommandConstants.SUCCESS;
        } else {

            double rate = ExchangeRateDatabase.getInstance().
                            getExchangeRate(currency, classicAcc.getCurrency());

            double newAmount = amount * rate;
            if (userSavingBalance < newAmount) {
                actionCode = CommandConstants.INSUFFICIENT_FUNDS;
                return;
            }

            handlePayment(user, userSavingAccount, classicAcc);
            actionCode = CommandConstants.SUCCESS;
        }
    }

    /**
     * This function checks if the user has a classic account to transfer the savings there
     * The search is done with the account type and currency as the main criteria
     * An error signal will be returned if the search failed
     * @param user The user that requested the command
     */
    public void checkClassicAccount(final User user) {

        for (Account acc : user.getUserAccounts().values()) {

            try {
                ((SavingAccount) acc).getInterestRate();
                continue;
            } catch (ClassCastException e) {

                if (acc.getCurrency().equals(currency)) {
                    checkFundsAccount(user, acc);
                    return;
                }
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
            return;
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
            return;
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        ObjectNode transactionNode = outputGenerator.getMapper().createObjectNode();
        String userEmail = outputGenerator.getUserDatabase().getMailEntry(account);
        User user = outputGenerator.getUserDatabase().getUserEntry(userEmail);

        switch (actionCode) {
            case SUCCESS:
                transactionNode.put("timestamp", timestamp);
                transactionNode.put("description", "Cash withdrawal of " + amount);
                transactionNode.put("amount", amount);
                user.addTransaction(transactionNode);

            case MINIMUM_AGE:
                transactionNode.put("description", "You don't have the minimum age required.");
                transactionNode.put("timestamp", timestamp);
                user.addTransaction(transactionNode);
                break;

            default :
                return;
        }
    }
}
