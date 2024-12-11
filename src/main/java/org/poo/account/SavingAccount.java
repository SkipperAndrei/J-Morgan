package org.poo.account;

import lombok.Data;

/**
 * This class will contain only objects that are Saving accounts
 * This type of Account will only have Interest Rate related transactions
 */
@Data
public class SavingAccount extends Account {

    private double interestRate;

    public SavingAccount(final String email, final String currency, final String accountType,
                         final int timestamp, final double interestRate) {
        super(email, currency, accountType, timestamp);
        this.interestRate = interestRate;
    }

}
