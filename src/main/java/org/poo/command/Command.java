package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.database.UserDatabase;
import org.poo.output.OutputGenerator;

public interface Command {
    public void executeCommand(UserDatabase userDatabase);
    public void generateOutput(OutputGenerator outputGenerator);
}