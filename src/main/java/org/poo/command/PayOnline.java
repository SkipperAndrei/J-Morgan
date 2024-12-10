package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.card.OneTimeCard;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class PayOnline implements Command {

    private static final int UNKNOWN_CURRENCY = -1;
    private static final int FROZEN_CARD = -2;
    private static final int INSUFFICIENT_FUNDS = -3;
    private static final int UNKNOWN_CARD = -4;
    private static final int SAVING_ACCOUNT = -5;
    private static final int POSSIBLE_TRANSACTION = 0;
    private static final int POSSIBLE_CARD_CHANGE = 1;

    private String cardNumber;
    private double originalAmount;
    private double amount;
    private String currency;
    private int timestamp;
    private String description;
    private String IBAN;
    private String commerciant;
    private String email;
    private int actionCode = UNKNOWN_CARD;
    private boolean changeCard = false;
    private final ExchangeRateDatabase exchangeRateDatabase;

    public PayOnline(CommandInput command, ExchangeRateDatabase exchangeRateDatabase) {
        cardNumber = command.getCardNumber();
        amount = command.getAmount();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
        description = command.getDescription();
        commerciant = command.getCommerciant();
        email = command.getEmail();
        originalAmount = amount;
        this.exchangeRateDatabase = exchangeRateDatabase;
    }


    public int paymentCheck(Account acc, Card card) {

        if (amount > acc.getBalance()) {
            return INSUFFICIENT_FUNDS;
        }

        acc.setBalance(acc.getBalance() - amount);

        try {
            ((OneTimeCard)card).getExpired();
            changeCard = true;
        } catch (ClassCastException e) {
            ;
        }
        return POSSIBLE_TRANSACTION;
    }

    public int cardCheck(Account acc) {

        Card card = acc.getCards().get(cardNumber);

        if (card.getStatus().toString().equals("frozen")) {
            return FROZEN_CARD;
        }

        return paymentCheck(acc, card);

    }

    public int currencyCheck(Account acc) {

        if (acc.getCurrency().equals(currency)) {
            return cardCheck(acc);
        }

        try {
            amount *= exchangeRateDatabase.getExchangeRate(currency, acc.getCurrency());
            return cardCheck(acc);
        } catch (NullPointerException e) {
            return UNKNOWN_CURRENCY;
        }

    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        for (Account acc : userDatabase.getUserEntry(email).getUserAccounts().values()) {

            if (acc.getCards().containsKey(cardNumber)) {
                IBAN = acc.getIban();
                actionCode = currencyCheck(acc);
            }
        }

    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        switch (actionCode) {

            case UNKNOWN_CARD :

                outputGenerator.errorSetting(timestamp, "Card not found", "payOnline");
                return;

            case INSUFFICIENT_FUNDS:

                ObjectNode errorNode = outputGenerator.getMapper().createObjectNode();
                errorNode.put("timestamp", timestamp);
                errorNode.put("description", "Insufficient funds");
                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(errorNode);

                Account acc = outputGenerator.getUserDatabase().getUserEntry(email).getUserAccounts().get(IBAN);
                outputGenerator.tryToAddTransaction(acc, errorNode);
                return;

            case FROZEN_CARD:

                ObjectNode frozenNode = outputGenerator.getMapper().createObjectNode();
                frozenNode.put("timestamp", timestamp);
                frozenNode.put("description", "The card is frozen");
                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(frozenNode);

                Account affectedAcc = outputGenerator.getUserDatabase().getUserEntry(email).getUserAccounts().get(IBAN);
                outputGenerator.tryToAddTransaction(affectedAcc, frozenNode);
                return;

            case POSSIBLE_TRANSACTION:

                ObjectNode paymentNode = outputGenerator.getMapper().createObjectNode();
                paymentNode.put("timestamp", timestamp);
                paymentNode.put("description", "Card payment");
                paymentNode.put("amount", amount);
                paymentNode.put("commerciant", commerciant);

                Account transAcc = outputGenerator.getUserDatabase().getUserEntry(email).getUserAccounts().get(IBAN);

                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(paymentNode);
                outputGenerator.tryToAddTransaction(transAcc, paymentNode);
                break;

            default :
                return;

        }

        if (changeCard) {
            Account affectedAcc = outputGenerator.getUserDatabase().getUserEntry(email).
                                getUserAccounts().get(IBAN);

            Card affectedCard = affectedAcc.getCards().get(cardNumber);
            ObjectNode affectedCardNode = ((OneTimeCard) affectedCard).updateCardNumber(timestamp,
                                "The card has been destroyed", true);
            affectedCardNode.put("cardHolder", email);
            affectedCardNode.put("account", IBAN);
            affectedAcc.getCards().remove(cardNumber);
            affectedAcc.getCards().put(affectedCard.getCardNumber(), affectedCard);

            outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(affectedCardNode);

            ObjectNode createdCardNode = affectedCardNode.deepCopy();
            createdCardNode.put("description", "New card created");
            createdCardNode.put("card", affectedCard.getCardNumber());

            outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(createdCardNode);
        }

    }
}

