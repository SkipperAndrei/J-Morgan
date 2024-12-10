package org.poo.command;

import lombok.Data;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

@Data
public class PrintUsers implements Command {

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
