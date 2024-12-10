package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

import java.util.List;
import java.util.ListIterator;

public class SplitPayment implements Command {

    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    private List<String> args;
    private double amountPerAccount;
    private double amount;
    private String currency;
    private String badAccount;
    private int timestamp;
    private int actionCode = SUCCESS;
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

    public void makePayment(final UserDatabase userDatabase, final String iban) {
        String email = userDatabase.getMailEntry(iban);
        Account acc = userDatabase.getUserEntry(email).getUserAccounts().get(iban);
        double amountToPay = amountPerAccount;

        amountToPay *= exchangeRateDatabase.getExchangeRate(currency, acc.getCurrency());
        userDatabase.getUserEntry(email).getUserAccounts().get(iban).decrementFunds(amountToPay);
    }

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
                actionCode = FAILURE;
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

            if (actionCode == FAILURE) {
                userSuccessNode.put("error", "Account " + badAccount +
                                    " has insufficient funds for a split payment.");

            }

            outputGenerator.getUserDatabase().
                    getUserEntry(email).addTransaction(userSuccessNode);
            outputGenerator.tryToAddTransaction(acc, userSuccessNode);
        }

    }
}
