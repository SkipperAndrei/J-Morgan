package org.poo.account;

import lombok.Data;

@Data
public class SavingAccount extends Account {

    private double interestRate;

    public SavingAccount(final String email, final String currency, final String accountType,
                         final int timestamp, final double interestRate) {
        super(email, currency, accountType, timestamp);
        this.interestRate = interestRate;
    }

}
