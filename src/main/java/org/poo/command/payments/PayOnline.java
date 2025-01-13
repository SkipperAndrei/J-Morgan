package org.poo.command.payments;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.AccountPlans;
import org.poo.account.BusinessAccount;
import org.poo.card.Card;
import org.poo.card.OneTimeCard;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
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
    private CommandConstants upgraded = CommandConstants.NOT_FOUND;
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

        if (acc.getAccountType().equals("business")) {
            boolean belowLimit = ((BusinessAccount) acc).checkPayment(amount, email,
                                    commerciant, timestamp);

            if (!belowLimit) {
                return CommandConstants.NO_PERMISSION;
            }
        }
        System.out.println("Contul involved este " + acc.getIban());
        System.out.println("Balance ul before " + acc.getBalance() + " la timestamp " + timestamp);
        System.out.println("Platim " + amount + " si cu comision " + actualAmount);
        User user = UserDatabase.getInstance().getUserEntry(email);
        acc.decrementFunds(actualAmount);

        if (acc.getPlan().equals(AccountPlans.SILVER)) {

            if (acc.getCurrency().equals("ron") || currency.equals("RON")) {

                int bigPayments = user.getBigPayments();
                bigPayments = actualAmount >= 300 ? bigPayments + 1 : bigPayments;
                user.setBigPayments(bigPayments);

            } else {

                double rate = ExchangeRateDatabase.getInstance().getExchangeRate(currency, "RON");
                double checkedAmount = actualAmount * rate;
                int bigPayments = user.getBigPayments();
                bigPayments = checkedAmount >= 300 ? bigPayments + 1 : bigPayments;

                user.setBigPayments(bigPayments);
            }

            if (user.getBigPayments() == 5) {
                upgraded = CommandConstants.SUCCESS;
//                user.upgradePlanTrans(acc.getIban(), timestamp, "gold");
                user.upgradeAllPlans("gold");
            }
        }

        acc.handleCommerciantPayment(commerciant, amount);

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

//        if (!card.getCardOwner().equals(email))
//            return CommandConstants.UNKNOWN_CARD;

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

        if (amount == 0)
            return;

        for (Account acc : userDatabase.getUserEntry(email).getUserAccounts().values()) {

            if (acc.getCards().containsKey(cardNumber)) {
                iban = acc.getIban();
                actionCode = currencyCheck(acc);
            }
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        if (amount == 0)
            return;

        User user = UserDatabase.getInstance().getUserEntry(email);
        Account acc = user.getUserAccounts().get(iban);

        switch (actionCode) {

            case UNKNOWN_CARD:

                outputGenerator.errorSetting(timestamp, "Card not found", "payOnline");
                return;

            case INSUFFICIENT_FUNDS:

                ObjectNode errorNode = outputGenerator.getMapper().createObjectNode();
                errorNode.put("timestamp", timestamp);
                errorNode.put("description", "Insufficient funds");
                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(errorNode);



                outputGenerator.tryToAddTransaction(acc, errorNode);
                return;

            case FROZEN_CARD:

                ObjectNode frozenNode = outputGenerator.getMapper().createObjectNode();
                frozenNode.put("timestamp", timestamp);
                frozenNode.put("description", "The card is frozen");
                user.addTransaction(frozenNode);


                outputGenerator.tryToAddTransaction(acc, frozenNode);
                return;

//            case NO_PERMISSION:
//
//                outputGenerator.errorSetting(timestamp,
//                                "You are not authorized to make this transaction.", "payOnline");
//                break;

            case SUCCESS:

                ObjectNode paymentNode = outputGenerator.getMapper().createObjectNode();
                paymentNode.put("timestamp", timestamp);
                paymentNode.put("description", "Card payment");
                paymentNode.put("amount", amount);
                paymentNode.put("commerciant", commerciant);

                user.addTransaction(paymentNode);
                outputGenerator.tryToAddTransaction(acc, paymentNode);

                if (upgraded.equals(CommandConstants.SUCCESS)) {
                    user.upgradePlanTrans(iban, timestamp, "gold");
                }

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

