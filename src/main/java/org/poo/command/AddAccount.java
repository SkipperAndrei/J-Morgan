package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.account.SavingAccount;
import org.poo.database.UserDatabase;
import lombok.Data;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

@Data
public final class AddAccount implements Command {

    private String email;
    private String currency;
    private String accountType;
    private String description;
    private int timestamp;
    private double interestRate;
    private String newIban;

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

        switch (accountType) {
            case "classic":
                newAccount = new Account(email, currency, accountType, timestamp,
                        userDB.getUserEntry(email).getUserData().getOccupation());
                break;

            case "savings":
                newAccount = new SavingAccount(email, currency, accountType, timestamp, interestRate,
                        userDB.getUserEntry(email).getUserData().getOccupation());
                break;

            case "business":
                newAccount = new BusinessAccount(email, currency, accountType, timestamp,
                        userDB.getUserEntry(email).getUserData().getOccupation());
                break;

            default:
                return;
        }

        newIban = newAccount.getIban();

        if (!userDB.getUserEntry(email).getUserAccounts().isEmpty()) {
            newAccount.setPlan(userDB.getUserEntry(email).getUserAccounts().
                                        values().iterator().next().getPlan());

        }

        userDB.getUserEntry(email).addAccount(newAccount);
        userDB.addMailEntry(newIban, email);
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        ObjectNode newAccountNode = outputGenerator.getMapper().createObjectNode();
        newAccountNode.put("timestamp", timestamp);
        newAccountNode.put("description", "New account created");
        outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(newAccountNode);

        Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                    getUserAccounts().get(newIban);
        outputGenerator.tryToAddTransaction(acc, newAccountNode);
    }
}
