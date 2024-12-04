package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import org.poo.database.UserDatabase;
import org.poo.output.OutputGenerator;

@Data
public class PrintUsers implements Command {

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        return;
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator, final int timestamp) {
        outputGenerator.addUsers(timestamp);
    }
}
