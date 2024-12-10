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
    private String accountType;
    private int timestamp;
    private String iban;
    private double balance;
    private double minimumBalance;
    private Map<String, Card> cards = new LinkedHashMap<>();
    private ArrayNode accountTransactions;

    public Account(final String email, final String currency,
                   final String accountType, final int timestamp) {
        this.email = email;
        this.currency = currency;
        this.accountType = accountType;
        this.timestamp = timestamp;
        balance = 0;
        minimumBalance = 0;
        iban = Utils.generateIBAN();
        accountTransactions = new ObjectMapper().createArrayNode();
    }

    public void incrementFunds(final double amount) {
        balance += amount;
    }

    public void decrementFunds(final double amount) {
        balance -= amount;
    }

    public void addTransaction(ObjectNode transaction) {
        ObjectNode newTransaction = transaction.deepCopy();
        accountTransactions.add(newTransaction);
    }

    public boolean canPay(final double amount) {
        return !(balance < amount);
    }

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
