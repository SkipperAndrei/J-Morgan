package org.poo.command;

import org.poo.account.Account;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class SetAlias implements Command {

    private String email;
    private String account;
    private String alias;
    private int timestamp;

    public SetAlias(CommandInput command) {
        email = command.getEmail();
        account = command.getAccount();
        alias = command.getAlias();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        try {
            if (userDatabase.getDatabase().get(email).getUserAccounts().containsKey(account)) {
                Account acc = userDatabase.getDatabase().get(email).getUserAccounts().get(account);
                userDatabase.getDatabase().get(email).addAccountAlias(acc, alias);
            }
        } catch (NullPointerException e) {
            return;
        }
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        return;
    }
}
