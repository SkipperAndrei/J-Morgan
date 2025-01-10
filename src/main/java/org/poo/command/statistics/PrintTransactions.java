package org.poo.command.statistics;

import org.poo.command.Command;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class PrintTransactions implements Command {

    private String email;
    private int timestamp;

    public PrintTransactions(final CommandInput command) {

        email = command.getEmail();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {
        return;
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {
        outputGenerator.printTransaction(timestamp, outputGenerator.
                                                    getUserDatabase().getUserEntry(email));
    }
}
