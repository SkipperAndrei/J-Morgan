package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.SavingAccount;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;
import org.poo.user.User;

public class ChangeInterestRate implements Command {

    private static final int SUCCESS = 0;
    private static final int CLASSIC_ACC = -1;
    private static final int NOT_FOUND = -2;

    private String account;
    private double interestRate;
    private int timestamp;
    private String email;
    private int actionCode = NOT_FOUND;

    public ChangeInterestRate(final CommandInput command) {
        account = command.getAccount();
        interestRate = command.getInterestRate();
        timestamp = command.getTimestamp();
    }

    public void checkAccount(final Account acc) {

        try {
            ((SavingAccount) acc).setInterestRate(interestRate);
            actionCode = SUCCESS;
        } catch (ClassCastException e) {
            actionCode = CLASSIC_ACC;
        }
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        for (User user : userDatabase.getDatabase().values()) {

            if (user.getUserAccounts().containsKey(account)) {
                email = user.getUserData().getEmail();
                checkAccount(user.getUserAccounts().get(account));
                return;
            }
        }
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        switch (actionCode) {
            case SUCCESS:

                ObjectNode successNode = outputGenerator.getMapper().createObjectNode();
                successNode.put("timestamp", timestamp);
                successNode.put("description", "Interest rate of the account changed to "
                                + interestRate);

                outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(successNode);
                outputGenerator.getUserDatabase().getUserEntry(email).getUserAccounts().
                                get(account).addTransaction(successNode);
                break;

            case CLASSIC_ACC:

                outputGenerator.errorSetting(timestamp, "This is not a savings account",
                                    "changeInterestRate");
                break;

            case NOT_FOUND:

                outputGenerator.errorSetting(timestamp, "Account not found", "changeInterestRate");
                break;

            default:
                break;
        }
    }
}
