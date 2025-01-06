package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;
import org.poo.utils.SplitTracker;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Data
public final class SplitPayment implements Command {

    private List<String> args;
    private List<Double> amountsPerAccount;
    private double amountPerAccount;
    private double amount;
    private String currency;
    private String badAccount;
    private String type;
    private Map<String, Boolean> acceptedAccounts;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public SplitPayment(final CommandInput command,
                        final ExchangeRateDatabase exchangeRateDatabase) {

        args = command.getAccounts();
        amount = command.getAmount();
        type = command.getSplitPaymentType();
        if (type.equals("custom")) {
            amountsPerAccount = command.getAmountForUsers();
        } else {
            amountPerAccount = amount / args.size();
        }
        acceptedAccounts = new HashMap<>();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
    }

    /**
     * This function, that assumes that balance checking was done before calling, will
     * extract the necessary funds required to make the payment.
     * @param userDatabase The database that will be queried
     * @param iban The Iban of the account
     */
    public void makePayment(final UserDatabase userDatabase, final String iban) {
        String email = userDatabase.getMailEntry(iban);
        Account acc = userDatabase.getUserEntry(email).getUserAccounts().get(iban);
        double amountToPay = amountPerAccount;

        amountToPay *= ExchangeRateDatabase.getInstance().getExchangeRate(currency, acc.getCurrency());
        userDatabase.getUserEntry(email).getUserAccounts().get(iban).decrementFunds(amountToPay);
    }

    /**
     * This function will check if an account, identified by it's Iban, has enough funds to pay.
     * @param userDatabase The database that will be queried to get the account
     * @param arg The Iban of the account
     * @return True, if the account has enough funds, False otherwise
     */
    public boolean checkAccount(final UserDatabase userDatabase, final String arg) {

        String userEmail = userDatabase.getMailEntry(arg);
        Account acc = userDatabase.getUserEntry(userEmail).getUserAccounts().get(arg);

        if (acc.getCurrency().equals(currency)) {
            return acc.canPay(amountPerAccount);
        }

        try {
            double amountToPay = amountPerAccount;
            amountToPay *= ExchangeRateDatabase.getInstance().getExchangeRate(currency, acc.getCurrency());

            return acc.canPay(amountToPay);

        } catch (NullPointerException e) {
            return false;
        }

    }

    public void finallyExecuteCommand(final UserDatabase userDatabase) {

        ListIterator<String> argsIterator = args.listIterator();

        while (argsIterator.hasNext()) {

            // TODO: implement custom-type logic
            String arg = argsIterator.next();
            boolean valid = checkAccount(userDatabase, arg);

            if (!valid) {
                badAccount = arg;
                actionCode = CommandConstants.INSUFFICIENT_FUNDS;
                return;
            }
        }

        argsIterator = args.listIterator();
        while (argsIterator.hasNext()) {
            String argument = argsIterator.next();
            makePayment(userDatabase, argument);
        }

    }

    public void finallyGenerateOutput(final OutputGenerator outputGenerator) {

        ObjectNode successNode = outputGenerator.defaultSplitOutput(args, timestamp,
                currency, amount, amountsPerAccount, type);

        for (String iban : args) {
            ObjectNode userSuccessNode = successNode.deepCopy();
            String email = outputGenerator.getUserDatabase().getMailEntry(iban);
            Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                        getUserAccounts().get(iban);

            if (type.equals("equal")) {
                userSuccessNode.put("amount", amountPerAccount);
            }

            if (actionCode == CommandConstants.INSUFFICIENT_FUNDS) {
                userSuccessNode.put("error", "Account " + badAccount
                                    + " has insufficient funds for a split payment.");

            }

            outputGenerator.getUserDatabase().
                    getUserEntry(email).addTimestampTransaction(timestamp, userSuccessNode);
            outputGenerator.tryToAddTimestampTransaction(timestamp, acc, userSuccessNode);
        }

    }


    @Override
    public void executeCommand(UserDatabase userDatabase) {

        ListIterator<String> argsIterator = args.listIterator();

        while (argsIterator.hasNext()) {

            String Iban = argsIterator.next();
            acceptedAccounts.put(Iban, false);

            try {
                String userEmail = userDatabase.getMailEntry(Iban);

                if (userEmail == null) {
                    throw new NullPointerException();
                }

            } catch (NullPointerException e) {
                actionCode = CommandConstants.NOT_FOUND;
                return;
            }

        }

        SplitTracker.getInstance().getListener().add(this);
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        if (actionCode == CommandConstants.NOT_FOUND) {
            outputGenerator.errorSetting(timestamp, "One of the accounts is invalid", "splitPayment");
            return;
        }


    }
}
