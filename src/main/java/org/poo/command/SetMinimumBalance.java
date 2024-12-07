package org.poo.command;

import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;
import org.poo.user.User;

public class SetMinimumBalance implements Command {

    private String account;
    private double amount;
    private int timestamp;

    public SetMinimumBalance(CommandInput command) {
        account = command.getAccount();
        amount = command.getAmount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        for (User usr: userDatabase.getDatabase().values()) {
            if (usr.getUserAccounts().containsKey(account)) {
                usr.getUserAccounts().get(account).setMinimumBalance(amount);
                break;
            }
        }
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        return;
    }
}
