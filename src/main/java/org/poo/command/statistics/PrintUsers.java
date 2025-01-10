package org.poo.command.statistics;

import lombok.Data;
import org.poo.command.Command;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

@Data
public final class PrintUsers implements Command {

    private int timestamp;

    public PrintUsers(final CommandInput command) {
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {
        return;
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {
        outputGenerator.addUsers(timestamp);
    }
}
