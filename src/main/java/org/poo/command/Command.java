package org.poo.command;

import org.poo.database.UserDatabase;
import org.poo.output.OutputGenerator;

public interface Command {
    void executeCommand(UserDatabase userDatabase);
    void generateOutput(OutputGenerator outputGenerator);
}
