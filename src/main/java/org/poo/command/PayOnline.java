package org.poo.command;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.card.OneTimeCard;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;
import org.poo.utils.Utils;

public class PayOnline implements Command {

    private static final int UNKNOWN_CURRENCY = -1;
    private static final int FROZEN_CARD = -2;
    private static final int INSUFFICIENT_FUNDS = -3;
    private static final int UNKNOWN_CARD = -4;
    private static final int POSSIBLE_TRANSACTION = 0;

    private String cardNumber;
    private double amount;
    private String currency;
    private int timestamp;
    private String description;
    private String commerciant;
    private String email;
    private int actionCode = UNKNOWN_CARD;
    private final ExchangeRateDatabase exchangeRateDatabase;

    public PayOnline(CommandInput command, ExchangeRateDatabase exchangeRateDatabase) {
        cardNumber = command.getCardNumber();
        amount = command.getAmount();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
        description = command.getDescription();
        commerciant = command.getCommerciant();
        email = command.getEmail();
        this.exchangeRateDatabase = exchangeRateDatabase;
    }

    public int paymentCheck(Account acc, Card card) {

        if (amount > acc.getBalance()) {
            return INSUFFICIENT_FUNDS;
        }

        acc.setBalance(acc.getBalance() - amount);

        try {
            // TODO change this later
            ((OneTimeCard)card).getExpired();
            card.setCardNumber(Utils.generateCardNumber());
            card.changeCardStatus(acc);
        } catch (ClassCastException e) {
            card.changeCardStatus(acc);
        }

        return POSSIBLE_TRANSACTION;
    }

    public int cardCheck(Account acc) {

        Card card = acc.getCards().get(cardNumber);

        if (card.getStatus().equals("frozen")) {
            return FROZEN_CARD;
        }

        return paymentCheck(acc, card);

    }

    public int currencyCheck(Account acc) {

        if (acc.getCurrency().equals(currency)) {
            return cardCheck(acc);
        }

        if (exchangeRateDatabase.addUnknownExchange(currency, acc.getCurrency())) {
            DefaultWeightedEdge edge = exchangeRateDatabase.getExchangeGraph().getEdge(currency, acc.getCurrency());
            amount *= exchangeRateDatabase.getExchangeGraph().getEdgeWeight(edge);
            return cardCheck(acc);
        }

        return UNKNOWN_CURRENCY;
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        for (Account acc : userDatabase.getEntry(email).getUserAccounts().values()) {

            if (acc.getCards().containsKey(cardNumber)) {
                actionCode = currencyCheck(acc);
            }
        }

    }

    // TODO add other error fields for transactions
    // This method will be changed when we get to print Transactions
    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        switch (actionCode) {

            case UNKNOWN_CARD : {
                outputGenerator.errorPayment(timestamp, "Card not found");
            }

            default : {
                return;
            }
        }

    }
}

