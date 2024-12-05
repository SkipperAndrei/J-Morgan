package org.poo.account;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.card.Card;

@Data
public class SavingAccount extends Account {

    private double interestRate;

    public SavingAccount(final String email, final String currency, final String AccountType,
                         final int timestamp, final double interestRate) {
        super(email, currency, AccountType, timestamp);
        this.interestRate = interestRate;
    }

}
