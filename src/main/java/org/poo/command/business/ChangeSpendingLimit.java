package org.poo.command.business;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class ChangeSpendingLimit implements Command {

    private String email;
    private double newLimit;
    private String account;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public ChangeSpendingLimit(final CommandInput command) {
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
            boolean canChange = bussAcc.changeSpendingLimit(email, newLimit);

            if (!canChange) {
                actionCode = CommandConstants.NO_PERMISSION;
            }

        } catch (ClassCastException e) {

            // Doesn't matter if it's classic or savings account
            // It's not a business account
            actionCode = CommandConstants.CLASSIC_ACC;
        }
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        switch (actionCode) {

            case NO_PERMISSION:
                String message = "You must be owner in order to change spending limit.";
                outputGenerator.errorSetting(timestamp, message, "changeSpendingLimit");
                return;

            case CLASSIC_ACC:
                String error = "This is not a business account";
                outputGenerator.errorSetting(timestamp, error, "changeSpendingLimit");
                return;

            default:
                return;

        }
    }
}
