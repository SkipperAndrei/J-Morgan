package org.poo.account;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.poo.card.Card;
import org.poo.utils.Utils;
import java.util.ArrayList;

@Data
@NoArgsConstructor
public class Account {
    private String email;
    private String currency;
    private String AccountType;
    private int timestamp;
    private String IBAN;
    private double balance;
    private ArrayList<Card> cards = new ArrayList<>();

    public Account(final String email, final String currency, final String AccountType, final int timestamp) {
        this.email = email;
        this.currency = currency;
        this.AccountType = AccountType;
        this.timestamp = timestamp;
        balance = 0;
        IBAN = Utils.generateIBAN();
    }

    public void incrementFunds(final double amount) {
        balance += amount;
    }

    public void decrementFunds(final double amount) {
        balance -= amount;
    }

    public ObjectNode accountToJson(ObjectMapper mapper) {
        ObjectNode accountNode = mapper.createObjectNode();

        accountNode.put("IBAN", IBAN);
        accountNode.put("balance", balance);
        accountNode.put("currency", currency);
        accountNode.put("type", AccountType);

        ArrayNode creditCards = mapper.createArrayNode();
        for (Card card : cards) {
            creditCards.add(card.cardToJson(mapper));
        }

        accountNode.set("cards", creditCards);
        return accountNode;
    }
}
