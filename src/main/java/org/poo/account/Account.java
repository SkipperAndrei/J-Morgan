package org.poo.account;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.poo.card.Card;
import org.poo.utils.Utils;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class Account {

    private String email;
    private String currency;
    private String AccountType;
    private int timestamp;
    private String IBAN;
    private double balance;
    private double minimumBalance;
    private Map<String, Card> cards = new LinkedHashMap<>();
    private Map<String, Double> payments = new LinkedHashMap<>();
    private ArrayNode accountTransactions;

    public Account(final String email, final String currency, final String AccountType, final int timestamp) {
        this.email = email;
        this.currency = currency;
        this.AccountType = AccountType;
        this.timestamp = timestamp;
        balance = 0;
        minimumBalance = 0;
        IBAN = Utils.generateIBAN();
        accountTransactions = new ObjectMapper().createArrayNode();
    }

    public void incrementFunds(final double amount) {
        balance += amount;
    }

    public void decrementFunds(final double amount) {
        balance -= amount;
    }

    public void updatePayments(final String commerciant, final double amount) {

        if (payments.containsKey(commerciant)) {
            payments.put(commerciant, payments.get(commerciant) + amount);
        } else {
            payments.put(commerciant, amount);
        }
    }

    public void addTransaction(ObjectNode transaction) {
        ObjectNode newTransaction = transaction.deepCopy();
        accountTransactions.add(newTransaction);
    }

    public boolean canPay(final double amount) {
        return !(balance < amount);
    }

    public Card searchCardNumber(final String cardNumber) {
        for (Card card : cards.values()) {
            if (card.cardNumber.equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }

    public ObjectNode accountToJson(ObjectMapper mapper) {
        ObjectNode accountNode = mapper.createObjectNode();

        accountNode.put("IBAN", IBAN);
        accountNode.put("balance", balance);
        accountNode.put("currency", currency);
        accountNode.put("type", AccountType);

        ArrayNode creditCards = mapper.createArrayNode();
        for (Card card : cards.values()) {
            creditCards.add(card.cardToJson(mapper));
        }

        accountNode.set("cards", creditCards);
        return accountNode;
    }


}
