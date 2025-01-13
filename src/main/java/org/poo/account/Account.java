package org.poo.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.poo.card.Card;
import org.poo.database.CommerciantDatabase;
import org.poo.database.ExchangeRateDatabase;
import org.poo.fileio.CommerciantInput;
import org.poo.utils.CashbackTracker;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class will hold information about the user accounts
 * Can be of two types : classic and saving account
 */
@Data
@NoArgsConstructor
public class Account {

    private String email;
    private String currency;
    private String accountType;
    private int timestamp;
    private String iban;
    private double balance;
    private double minimumBalance;
    private AccountPlans plan = AccountPlans.STANDARD;

    private Map<String, Card> cards = new LinkedHashMap<>();
    private CashbackTracker cashTracker;
    private ArrayList<String> deletedOneTimeCards = new ArrayList<>();
    private ArrayNode accountTransactions;

    public Account(final String email, final String currency,
                   final String accountType, final int timestamp,
                   final String occupation) {
        this.email = email;
        this.currency = currency;
        this.accountType = accountType;
        this.timestamp = timestamp;

        if (occupation.equals("student")) {
            plan = AccountPlans.STUDENT;
        }

        balance = 0;
        minimumBalance = 0;
        iban = Utils.generateIBAN();
        cashTracker = new CashbackTracker();
        accountTransactions = new ObjectMapper().createArrayNode();
    }

    /**
     * This function will increment the balance of the account
     * @param amount The amount
     */
    public void incrementFunds(final double amount) {
        balance += amount;
    }

    /**
     * This function will decrement the balance of the account
     * Requires checking if the amount is bigger than the current balance before calling
     * @param amount The amount
     */
    public void decrementFunds(final double amount) {
        balance -= amount;
    }

    /**
     * This function adds a new transaction involving the account
     * @param transaction The transaction
     */
    public void addTransaction(final ObjectNode transaction) {
        ObjectNode newTransaction = transaction.deepCopy();
        accountTransactions.add(newTransaction);
    }

    /**
     * This method adds a new transaction at a certain position in the transactions logs
     * It should only be called in the Split Payment output generating
     * @param timeTimestamp The timestamp of the Split payment command
     * @param transaction The transaction
     */
    public void addTimedTransaction(final int timeTimestamp, final ObjectNode transaction) {

        Iterator<JsonNode> jsonIterator = accountTransactions.elements();
        int position = 0;

        while (jsonIterator.hasNext()) {

            ObjectNode jsonNode = (ObjectNode) jsonIterator.next();

            if (jsonNode.get("timestamp").asInt() < timeTimestamp) {
                position += 1;
            }

            if (jsonNode.get("timestamp").asInt() > timeTimestamp) {
                accountTransactions.insert(position, transaction);
                return;
            }

        }

        addTransaction(transaction);

    }

    /**
     * This function will check if the account has enough money in order to pay
     * @param amount The amount required to be paid
     * @return True, if it can pay, False otherwise
     */
    public boolean canPay(final double amount) {
        return !(balance < amount);
    }

    /**
     * This function is responsible for calculating the cashback in case of a commerciant payment
     * Firstly, it gets the id of the commerciant, based on the name/iban
     * After, it calculates cashback for commerciants of type NumberOfTransactions
     * Then checks if it is eligible for discounts in future payments
     * After this checks it calculates cashback for Spending Threshold commerciants
     * Finally, it adds the cashback for Nr.Transactions and spend Threshold to the account balance
     * @param receiver The receiver commerciant
     * @param amount The amount to pay
     */
    public void handleCommerciantPayment(final String receiver, final double amount) {

        Integer commId = CommerciantDatabase.getInstance().getCommIbanToId().get(receiver);

        if (commId == null) {
            commId = CommerciantDatabase.getInstance().getCommNameToId().get(receiver);
        }

        CommerciantInput commInfo = CommerciantDatabase.getInstance().getCommerciant(commId);
        double cashback = cashTracker.calculateNrTransactionsCashback(commInfo.getType(), amount);


        if (commInfo.getCashbackStrategy().equals("nrOfTransactions")) {

            int nrTrans = cashTracker.getNrOfTransCommerciants().get(commId) == null
                            ? 0 : cashTracker.getNrOfTransCommerciants().get(commId);

            cashTracker.getNrOfTransCommerciants().put(commId, nrTrans + 1);

            cashTracker.checkFoodDiscount(commId);
            cashTracker.checkClothesDiscount(commId);
            cashTracker.checkTechDiscount(commId);
        } else {

            double previousSpent = cashTracker.getSpendingCommerciants();
            double currencyRate = ExchangeRateDatabase.getInstance().
                                    getExchangeRate(currency, "RON");

            double newAmount = amount * currencyRate;

            cashTracker.setSpendingCommerciants(previousSpent + newAmount);

            double spCashback = cashTracker.SpendingTransCashback(newAmount,
                                            plan.getPlanStrategy());

            cashback += spCashback / currencyRate;

        }

        balance += cashback;

    }

    /**
     * This function maps the contents of the account in JSON format
     * @param mapper
     * @return The mapped JSON node
     */
    public ObjectNode accountToJson(final ObjectMapper mapper) {
        ObjectNode accountNode = mapper.createObjectNode();

        accountNode.put("IBAN", iban);
        accountNode.put("balance", balance);
        accountNode.put("currency", currency);
        accountNode.put("type", accountType);

        ArrayNode creditCards = mapper.createArrayNode();
        for (Card card : cards.values()) {
            creditCards.add(card.cardToJson(mapper));
        }

        accountNode.set("cards", creditCards);
        return accountNode;
    }

}
