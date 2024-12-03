package org.poo.account;
import lombok.Data;
import org.poo.card.Card;

import java.util.ArrayList;

@Data
public class Account {
    private String userEmail;
    private String currency;
    private String AccountType;
    private int timestamp;
    private String IBAN;
    private double balance;
    private ArrayList<Card> cards = new ArrayList<>();


}
