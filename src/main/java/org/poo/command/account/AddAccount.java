package org.poo.command.account;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.account.SavingAccount;
import org.poo.command.Command;
import org.poo.database.UserDatabase;
import lombok.Data;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

@Data
public final class AddAccount implements Command {

    private String email;
    private String currency;
    private String accType;
    private String description;
    private int timestamp;
    private double interestRate;
    private String newIban;

    public AddAccount(final CommandInput command) {

        email = command.getEmail();
        currency = command.getCurrency();
        timestamp = command.getTimestamp();
        accType = command.getAccountType();
        interestRate = command.getInterestRate();
        description = command.getDescription();
    }

    @Override
    public void executeCommand(final UserDatabase userDB) {

        Account newAccount;

        switch (accType) {
            case "classic":
                newAccount = new Account(email, currency, accType, timestamp,
                        userDB.getUserEntry(email).getUserData().getOccupation());
                break;

            case "savings":
                newAccount = new SavingAccount(email, currency, accType, timestamp, interestRate,
                            userDB.getUserEntry(email).getUserData().getOccupation());
                break;

            case "business":
                newAccount = new BusinessAccount(email, currency, accType, timestamp,
                        userDB.getUserEntry(email).getUserData().getOccupation());
                break;

            default:
                return;
        }

        newIban = newAccount.getIban();

        newAccount.setPlan(userDB.getUserEntry(email).getUserPlan());

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
        acc.addTransaction(newAccountNode);
    }
}
