package org.poo.command;

import org.poo.database.UserDatabase;
import org.poo.utils.OutputGenerator;

public interface Command {

    /**
     * This function will query the database and execute the commands
     * Also, this function does error checking on the queries
     * @param userDatabase The queried database
     */
    void executeCommand(UserDatabase userDatabase);

    /**
     * This function will map the output of the queries from the "executeCommand" function
     * @param outputGenerator Instance of the class that handles the generating of the output
     */
    void generateOutput(OutputGenerator outputGenerator);
}
