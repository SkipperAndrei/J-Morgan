package org.poo.command;

import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class PrintTransactions implements Command {

    private String email;
    private int timestamp;

    public PrintTransactions(CommandInput command) {
        email = command.getEmail();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        return;
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        outputGenerator.printTransaction(timestamp, outputGenerator.
                                                    getUserDatabase().getUserEntry(email));
    }
}
