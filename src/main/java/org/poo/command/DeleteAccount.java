package org.poo.command;

import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;
import org.poo.user.User;

public class DeleteAccount implements Command {

    private String email;
    private String account;
    private int timestamp;
    private boolean error = false;


    public DeleteAccount(CommandInput command) {
        email = command.getEmail();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        try {
            // userDatabase.getEntry(email).getUserAccounts().remove(account);
            if (userDatabase.getUserEntry(email).getUserAccounts().get(account).getBalance() != 0) {
                error = true;
            } else {
                userDatabase.getUserEntry(email).getUserAccounts().remove(account);
                userDatabase.removeMailEntry(account);
            }
        } catch (Exception ex) {
            error = true;
        }

    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        User user = outputGenerator.getUserDatabase().getUserEntry(email);
        outputGenerator.deleteAccount(timestamp, error, user);
    }
}
