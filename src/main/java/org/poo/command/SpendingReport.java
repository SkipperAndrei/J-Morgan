package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.SavingAccount;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class SpendingReport implements Command {

    private static final  int SUCCESS = 0;
    private static final  int SAVING_ACC = -1;
    private static final  int UNKNOWN_ACC = -2;

    private int startTimestamp;
    private int endTimestamp;
    private String account;
    private String email;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public SpendingReport(final CommandInput command) {
        startTimestamp = command.getStartTimestamp();
        endTimestamp = command.getEndTimestamp();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        email = userDatabase.getMailEntry(account);

        if (email == null) {
            actionCode = CommandConstants.NOT_FOUND;
            return;
        }

        try {
            Account acc = userDatabase.getUserEntry(email).getUserAccounts().get(account);
            ((SavingAccount) acc).getInterestRate();
            actionCode = CommandConstants.SAVING_ACC;
        } catch (ClassCastException | NullPointerException e) {
            return;
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        switch (actionCode) {

            case SAVING_ACC:
                ObjectNode savingAccNode = outputGenerator.getMapper().createObjectNode();
                savingAccNode.put("command", "spendingsReport");
                ObjectNode errorNode = outputGenerator.getMapper().createObjectNode();
                errorNode.put("error", "This kind of report is not supported "
                            + "for a saving account");
                savingAccNode.set("output", errorNode);
                savingAccNode.put("timestamp", timestamp);
                outputGenerator.getOutput().add(savingAccNode);
                return;

            case NOT_FOUND:
                outputGenerator.errorSetting(timestamp, "Account not found", "spendingsReport");
                return;

            case SUCCESS:
                outputGenerator.generateSpendingReport(startTimestamp, endTimestamp,
                                email, account, timestamp);
                return;

            default:
                break;
        }
    }
}
