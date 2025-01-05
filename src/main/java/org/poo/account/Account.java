package org.poo.account;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.poo.card.Card;
import org.poo.database.CommerciantDatabase;
import org.poo.database.ExchangeRateDatabase;
import org.poo.fileio.CommerciantInput;
import org.poo.plans.Plan;
import org.poo.utils.CashbackTracker;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
//    private Map<String, Double> spendingCommerciants = new LinkedHashMap<>();
//    private Map<String, Integer> nrOfTransCommerciants = new LinkedHashMap<>();
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
     * This function will check if the account has enough money in order to pay
     * @param amount The amount required to be paid
     * @return
     */
    public boolean canPay(final double amount) {
        return !(balance < amount);
    }

    public void handleCommerciantPayment(final String receiver, final double amount) {


        Integer commId = CommerciantDatabase.getInstance().getCommIbanToId().get(receiver);

        if (commId == null) {
            commId = CommerciantDatabase.getInstance().getCommNameToId().get(receiver);
        }

        CommerciantInput commInfo = CommerciantDatabase.getInstance().getCommerciant(commId);
        // System.out.println("Email " + email + " platesc la " + commInfo.getCommerciant());
        double cashback = cashTracker.calculateNrTransactionsCashback(commInfo.getType(), amount);

        if (commInfo.getCashbackStrategy().equals("nrOfTransactions")) {

            int nrTrans = cashTracker.getNrOfTransCommerciants().get(commId) == null ?
                            0 : cashTracker.getNrOfTransCommerciants().get(commId);

            cashTracker.getNrOfTransCommerciants().put(commId, nrTrans + 1);

            cashTracker.checkFoodDiscount(commId);
            cashTracker.checkClothesDiscount(commId);
            cashTracker.checkTechDiscount(commId);
        } else {

            // System.out.println("Am ceva pe aici? " + cashTracker.getSpendingCommerciants().get(commId));
            double previousSpent = cashTracker.getSpendingCommerciants().get(commId) == null ?
                                    0 : cashTracker.getSpendingCommerciants().get(commId);


            double currencyRate = ExchangeRateDatabase.getInstance().getExchangeRate(currency, "RON");
            double newAmount = amount * currencyRate;

            cashTracker.getSpendingCommerciants().put(commId, previousSpent + newAmount);

            // System.out.println("Am cheltuit la " + receiver + " suma de " + cashTracker.getSpendingCommerciants().get(commId));
            Plan accPlan = plan.getPlanStrategy();
            double spCashback = cashTracker.SpendingTransCashback(newAmount,
                                                commId, plan.getPlanStrategy());

            cashback += spCashback / currencyRate;
        }

//        System.out.println("Email " + email + " are " + balance);
//        System.out.println("Cashback-ul este " + cashback);
        balance += cashback;

//        System.out.println("Email " + email + " are acum " + balance);

    }

    /**
     * This function maps the contents of the account in JSON format
     * @param mapper
     * @return The mapped JSON node
     */
    public ObjectNode accountToJson(final ObjectMapper mapper) {
        ObjectNode accountNode = mapper.createObjectNode();

        accountNode.put("IBAN", iban);
        accountNode.put("balance", Math.round(balance * 100.0) / 100.0);
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
