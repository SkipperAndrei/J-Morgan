package org.poo.command;

import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;
import org.poo.utils.SplitTracker;

public class RejectSplitPayment implements Command {

    private String email;
    private String type;
    private int timestamp;

    public RejectSplitPayment(CommandInput command) {

        email = command.getEmail();
        type = command.getType();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        SplitPayment payment = SplitTracker.getInstance().reject(email);
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        return;
    }
}
