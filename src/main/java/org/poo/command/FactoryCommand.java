package org.poo.command;

import org.poo.command.account.AddAccount;
import org.poo.command.account.AddFunds;
import org.poo.command.account.AddInterest;
import org.poo.command.account.CashWithdrawal;
import org.poo.command.account.ChangeInterestRate;
import org.poo.command.account.DeleteAccount;
import org.poo.command.account.SetAlias;
import org.poo.command.account.SetMinimumBalance;
import org.poo.command.account.UpgradePlan;
import org.poo.command.account.WithdrawSavings;
import org.poo.command.business.AddNewBusinessAssociate;
import org.poo.command.business.BusinessReport;
import org.poo.command.business.ChangeDepositLimit;
import org.poo.command.business.ChangeSpendingLimit;
import org.poo.command.cards.CheckCardStatus;
import org.poo.command.cards.CreateCard;
import org.poo.command.cards.CreateOneTimeCard;
import org.poo.command.cards.DeleteCard;
import org.poo.command.payments.AcceptSplitPayment;
import org.poo.command.payments.PayOnline;
import org.poo.command.payments.RejectSplitPayment;
import org.poo.command.payments.SendMoney;
import org.poo.command.payments.SplitPayment;
import org.poo.command.statistics.PrintTransactions;
import org.poo.command.statistics.PrintUsers;
import org.poo.command.statistics.Report;
import org.poo.command.statistics.SpendingReport;
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
