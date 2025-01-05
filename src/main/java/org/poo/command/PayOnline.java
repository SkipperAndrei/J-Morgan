package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.card.OneTimeCard;
import org.poo.database.CommerciantDatabase;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class PayOnline implements Command {

    private String cardNumber;
    private double amount;
    private String currency;
    private int timestamp;
    private String iban;
    private String commerciant;
    private String email;
    private CommandConstants actionCode = CommandConstants.UNKNOWN_CARD;
    private boolean changeCard = false;
    private final ExchangeRateDatabase exchangeRateDatabase;

    public PayOnline(final CommandInput command, final ExchangeRateDatabase exchangeRateDatabase) {

        cardNumber = command.getCardNumber();
        amount = command.getAmount();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
        commerciant = command.getCommerciant();
        email = command.getEmail();
        this.exchangeRateDatabase = exchangeRateDatabase;

    }

    /**
     * This function checks if the account has enough funds to make the payment.
     * If the payment was done with a One-time card,
     * it sets the flag to generate another card number.
     * This new card number will be generated in the "generateOutput" function.
     * @param acc The affected account
     * @param card The card used to make the transaction
     * @return A signal code
     */
    public CommandConstants paymentCheck(final Account acc, final Card card) {

        double actualAmount = acc.getPlan().getPlanStrategy().
                            commissionStrategy(amount, acc.getCurrency());

        if (actualAmount > acc.getBalance()) {
            return CommandConstants.INSUFFICIENT_FUNDS;
        }

        // System.out.println("Timestamp " + timestamp + " user -ul " + email + " a platit " + actualAmount);
        acc.decrementFunds(actualAmount);
        acc.handleCommerciantPayment(commerciant, amount);

        // System.out.println("Dupa plata de la timestamp " + timestamp + " user-ul " + email + " mai are " + acc.getBalance());

        try {
            ((OneTimeCard) card).getExpired();
            changeCard = true;
        } catch (ClassCastException e) {
            return CommandConstants.SUCCESS;
        }
        return CommandConstants.SUCCESS;
    }

    /**
     * This function checks if the card used to make the payment is frozen or not
     * @param acc The affected account
     * @return A signal code
     */
    public CommandConstants cardCheck(final Account acc) {

        Card card = acc.getCards().get(cardNumber);

        if (card.getStatus().toString().equals("frozen")) {
            return CommandConstants.FROZEN_CARD;
        }

        return paymentCheck(acc, card);

    }

    /**
     * This function makes the conversion of the account currency
     * The currency that is converted to is the one utilized in the payment
     * @param acc The affected account
     * @return A signal code
     */
    public CommandConstants currencyCheck(final Account acc) {

        if (acc.getCurrency().equals(currency)) {
            return cardCheck(acc);
        }

        try {
            amount *= exchangeRateDatabase.getExchangeRate(currency, acc.getCurrency());
            return cardCheck(acc);
        } catch (NullPointerException e) {
            return CommandConstants.UNKNOWN_CURRENCY;
        }

    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        for (Account acc : userDatabase.getUserEntry(email).getUserAccounts().values()) {

            if (acc.getCards().containsKey(cardNumber)) {
                iban = acc.getIban();
                actionCode = currencyCheck(acc);
            }
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        switch (actionCode) {

            case UNKNOWN_CARD:

                outputGenerator.errorSetting(timestamp, "Card not found", "payOnline");
                return;

            case INSUFFICIENT_FUNDS:

                ObjectNode errorNode = outputGenerator.getMapper().createObjectNode();
                errorNode.put("timestamp", timestamp);
                errorNode.put("description", "Insufficient funds");
                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(errorNode);

                Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                                getUserAccounts().get(iban);

                outputGenerator.tryToAddTransaction(acc, errorNode);
                return;

            case FROZEN_CARD:

                ObjectNode frozenNode = outputGenerator.getMapper().createObjectNode();
                frozenNode.put("timestamp", timestamp);
                frozenNode.put("description", "The card is frozen");
                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(frozenNode);

                Account affectedAcc = outputGenerator.getUserDatabase().getUserEntry(email).
                                    getUserAccounts().get(iban);

                outputGenerator.tryToAddTransaction(affectedAcc, frozenNode);
                return;

            case SUCCESS:

                ObjectNode paymentNode = outputGenerator.getMapper().createObjectNode();
                paymentNode.put("timestamp", timestamp);
                paymentNode.put("description", "Card payment");
                paymentNode.put("amount", amount);
                paymentNode.put("commerciant", commerciant);

                Account transAcc = outputGenerator.getUserDatabase().getUserEntry(email).
                                getUserAccounts().get(iban);

                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(paymentNode);
                outputGenerator.tryToAddTransaction(transAcc, paymentNode);
                break;

            default :
                return;

        }

        if (changeCard) {
            Account affectedAcc = outputGenerator.getUserDatabase().getUserEntry(email).
                                getUserAccounts().get(iban);

            Card affectedCard = affectedAcc.getCards().get(cardNumber);
            affectedAcc.getDeletedOneTimeCards().add(cardNumber);
            ObjectNode affectedCardNode = ((OneTimeCard) affectedCard).updateCardNumber(timestamp,
                                "The card has been destroyed", true);
            affectedCardNode.put("cardHolder", email);
            affectedCardNode.put("account", iban);
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

