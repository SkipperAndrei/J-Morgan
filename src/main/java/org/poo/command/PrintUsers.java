package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

@Data
public class PrintUsers implements Command {

    private int timestamp;

    public PrintUsers(CommandInput command) {
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        return;
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        outputGenerator.addUsers(timestamp);
    }
}
