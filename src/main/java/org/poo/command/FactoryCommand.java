package org.poo.command;

import org.poo.database.ExchangeRateDatabase;
import org.poo.fileio.CommandInput;

public final class FactoryCommand {

    private FactoryCommand() {
        throw new UnsupportedOperationException("This is a utility class");
    }

    /**
     * This function will generate an instance of a class that implements the Command interface
     * The generation will be based on the type of command that is required
     * @param command The command object that contains all the information necessary to execute it
     * @param exchangeRateDatabase required in commands that handle payments
     * @return The instance of the class that implements the query
     */
    public static Command extractCommand(final CommandInput command,
                                         final ExchangeRateDatabase exchangeRateDatabase) {

        switch (command.getCommand()) {

            case "printUsers" -> {
                return new PrintUsers(command);
            }

            case "addAccount" -> {
                return new AddAccount(command);
            }

            case "deleteAccount" -> {
                return new DeleteAccount(command);
            }

            case "createCard" -> {
                return new CreateCard(command);
            }

            case "createOneTimeCard" -> {
                return new CreateOneTimeCard(command);
            }

            case "deleteCard" -> {
                return new DeleteCard(command);
            }

            case "setMinimumBalance" -> {
                return new SetMinimumBalance(command);
            }

            case "addFunds" -> {
                return new AddFunds(command);
            }

            case "payOnline" -> {
                return new PayOnline(command, exchangeRateDatabase);
            }

            case "sendMoney" -> {
                return new SendMoney(command, exchangeRateDatabase);
            }

            case "splitPayment" -> {
                return new SplitPayment(command);
            }

            case "setAlias" -> {
                return new SetAlias(command);
            }

            case "checkCardStatus" -> {
                return new CheckCardStatus(command);
            }

            case "printTransactions" -> {
                return new PrintTransactions(command);
            }

            case "changeInterestRate" -> {
                return new ChangeInterestRate(command);
            }

            case "addInterest" -> {
                return new AddInterest(command);
            }

            case "report" -> {
                return new Report(command);
            }

            case "spendingsReport" -> {
                return new SpendingReport(command);
            }

            case "withdrawSavings" -> {
                return new WithdrawSavings(command);
            }

            case "cashWithdrawal" -> {
                return new CashWithdrawal(command);
            }

            case "upgradePlan" -> {
                return new UpgradePlan(command);
            }

            case "acceptSplitPayment" -> {
                return new AcceptSplitPayment(command);
            }

            case "rejectSplitPayment" -> {
                return new RejectSplitPayment(command);
            }

            case "addNewBusinessAssociate" -> {
                return new AddNewBusinessAssociate(command);
            }

            case "changeSpendingLimit" -> {
                return new ChangeSpendingLimit(command);
            }

            case "changeDepositLimit" -> {
                return new ChangeDepositLimit(command);
            }

            case "businessReport" -> {
                return new BusinessReport(command);
            }

            default -> {
                return null;
            }

        }
    }
}
