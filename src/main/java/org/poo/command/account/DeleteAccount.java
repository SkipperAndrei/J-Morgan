package org.poo.command.account;

import org.poo.command.Command;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;
import org.poo.user.User;

public final class DeleteAccount implements Command {

    private String email;
    private String account;
    private int timestamp;
    private boolean error = false;


    public DeleteAccount(final CommandInput command) {
        email = command.getEmail();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        try {
            if (userDatabase.getUserEntry(email).getUserAccounts().get(account).getBalance() != 0) {
                error = true;
            } else {
                userDatabase.getUserEntry(email).getUserAccounts().get(account).getCards().clear();
                userDatabase.getUserEntry(email).getUserAccounts().remove(account);
                userDatabase.removeMailEntry(account);
            }
        } catch (Exception ex) {
            error = true;
        }

    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {
        User user = outputGenerator.getUserDatabase().getUserEntry(email);
        outputGenerator.deleteAccount(timestamp, error, user);
    }
}
