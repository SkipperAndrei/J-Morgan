package org.poo.command.business;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class ChangeDepositLimit implements Command {

    private String email;
    private double newLimit;
    private String account;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public ChangeDepositLimit(final CommandInput command) {
        email = command.getEmail();
        newLimit = command.getAmount();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        try {
            Account acc = userDatabase.getUserEntry(email).getUserAccounts().get(account);
            BusinessAccount bussAcc = (BusinessAccount) acc;
            boolean canChange = bussAcc.changeDepositLimit(email, newLimit);

            if (!canChange) {
                actionCode = CommandConstants.NO_PERMISSION;
            }

        } catch (ClassCastException e) {
            return;
        }
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        if (actionCode == CommandConstants.NO_PERMISSION) {
            String message = "You must be owner in order to change deposit limit.";
            outputGenerator.errorSetting(timestamp, message, "changeDepositLimit");

        }
    }
}
