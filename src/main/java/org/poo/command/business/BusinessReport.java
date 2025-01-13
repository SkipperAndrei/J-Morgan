package org.poo.command.business;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.OutputGenerator;

public final class BusinessReport implements Command {

    private String type;
    private int startTimestamp;
    private int endTimestamp;
    private String account;
    private String email;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public BusinessReport(final CommandInput command) {

        type = command.getType();
        startTimestamp = command.getStartTimestamp();
        endTimestamp = command.getEndTimestamp();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        try {

            email = userDatabase.getMailEntry(account);
            User user = userDatabase.getUserEntry(email);
            Account acc = user.getUserAccounts().get(account);
            ((BusinessAccount) acc).getDepositLimit();

        } catch (NullPointerException e) {

            actionCode = CommandConstants.NOT_FOUND;
        } catch (ClassCastException e) {

            // Doesn't matter if it's a classic account or a savings account
            // It only matters that it isn't a business account
            actionCode = CommandConstants.CLASSIC_ACC;
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        switch (actionCode) {

            case NOT_FOUND:
                outputGenerator.errorSetting(timestamp, "Account not found", "businessReport");
                break;

            case CLASSIC_ACC:
                outputGenerator.errorSetting(timestamp,
                                    "Account is not of type business", "businessReport");
                break;

            case SUCCESS:
                outputGenerator.generateBusinessReport(startTimestamp, endTimestamp, timestamp, type, account, email);
                break;

            default:
                break;
        }
    }
}
