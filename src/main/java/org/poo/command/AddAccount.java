package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.account.Account;
import org.poo.account.SavingAccount;
import org.poo.database.UserDatabase;
import lombok.Data;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

@Data
public class AddAccount implements Command {
    private String email;
    private String currency;
    private String accountType;
    private String description;
    private int timestamp;
    private double interestRate;

    public AddAccount(CommandInput command) {
        email = command.getEmail();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
        accountType = command.getAccountType();
        interestRate = command.getInterestRate();
        description = command.getDescription();
    }

    @Override
    public void executeCommand(UserDatabase userDB) {
        Account newAccount;
        if (accountType.equals("classic")) {
            newAccount = new Account(email, currency, accountType, timestamp);
        } else {
            newAccount = new SavingAccount(email, currency, accountType, timestamp, interestRate);
        }
        userDB.getEntry(email).addAccount(newAccount);
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator, final int timestamp) {
        return;
    }
}
