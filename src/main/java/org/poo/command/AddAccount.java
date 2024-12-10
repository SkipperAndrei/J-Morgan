package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private String newIBAN;

    public AddAccount(final CommandInput command) {
        email = command.getEmail();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
        accountType = command.getAccountType();
        interestRate = command.getInterestRate();
        description = command.getDescription();
    }

    @Override
    public void executeCommand(final UserDatabase userDB) {
        Account newAccount;
        if (accountType.equals("classic")) {
            newAccount = new Account(email, currency, accountType, timestamp);
        } else {
            newAccount = new SavingAccount(email, currency, accountType, timestamp, interestRate);
        }
        newIBAN = newAccount.getIban();
        userDB.getUserEntry(email).addAccount(newAccount);
        userDB.addMailEntry(newIBAN, email);
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        ObjectNode newAccountNode = outputGenerator.getMapper().createObjectNode();
        newAccountNode.put("timestamp", timestamp);
        newAccountNode.put("description", "New account created");
        outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(newAccountNode);
        Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                    getUserAccounts().get(newIBAN);
        outputGenerator.tryToAddTransaction(acc, newAccountNode);
    }
}
