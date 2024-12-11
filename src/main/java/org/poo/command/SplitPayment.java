package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

import java.util.List;
import java.util.ListIterator;

public final class SplitPayment implements Command {

    private List<String> args;
    private double amountPerAccount;
    private double amount;
    private String currency;
    private String badAccount;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;
    private ExchangeRateDatabase exchangeRateDatabase;


    public SplitPayment(final CommandInput command,
                        final ExchangeRateDatabase exchangeRateDatabase) {

        args = command.getAccounts();
        amount = command.getAmount();
        amountPerAccount = amount / args.size();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
        this.exchangeRateDatabase = exchangeRateDatabase;
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

        amountToPay *= exchangeRateDatabase.getExchangeRate(currency, acc.getCurrency());
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
            amountToPay *= exchangeRateDatabase.getExchangeRate(currency, acc.getCurrency());

            return acc.canPay(amountToPay);

        } catch (NullPointerException e) {
            return false;
        }

    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        ListIterator<String> argsIterator = args.listIterator(args.size());

        while (argsIterator.hasPrevious()) {

            String arg = argsIterator.previous();
            boolean valid = checkAccount(userDatabase, arg);

            if (!valid) {
                badAccount = arg;
                actionCode = CommandConstants.INSUFFICIENT_FUNDS;
                return;
            }
        }

        argsIterator = args.listIterator(args.size());
        while (argsIterator.hasPrevious()) {
            String argument = argsIterator.previous();
            makePayment(userDatabase, argument);
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        ObjectNode successNode = outputGenerator.defaultSplitOutput(args, timestamp,
                                currency, amount);

        for (String iban : args) {
            ObjectNode userSuccessNode = successNode.deepCopy();
            String email = outputGenerator.getUserDatabase().getMailEntry(iban);
            Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                        getUserAccounts().get(iban);

            userSuccessNode.put("amount", amountPerAccount);

            if (actionCode == CommandConstants.INSUFFICIENT_FUNDS) {
                userSuccessNode.put("error", "Account " + badAccount
                                    + " has insufficient funds for a split payment.");

            }

            outputGenerator.getUserDatabase().
                    getUserEntry(email).addTransaction(userSuccessNode);
            outputGenerator.tryToAddTransaction(acc, userSuccessNode);
        }

    }
}
