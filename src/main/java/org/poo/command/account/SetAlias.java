package org.poo.command.account;

import org.poo.account.Account;
import org.poo.command.Command;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class SetAlias implements Command {

    private String email;
    private String account;
    private String alias;

    public SetAlias(final CommandInput command) {
        email = command.getEmail();
        account = command.getAccount();
        alias = command.getAlias();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

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
    public void generateOutput(final OutputGenerator outputGenerator) {
        return;
    }
}
