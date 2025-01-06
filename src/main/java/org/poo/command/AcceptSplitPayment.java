package org.poo.command;

import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;
import org.poo.utils.SplitTracker;

public class AcceptSplitPayment implements Command {


    private String email;
    private String type;
    private int timestamp;
    private SplitPayment payment;

    public AcceptSplitPayment(CommandInput command) {

        email = command.getEmail();
        type = command.getType();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        payment = SplitTracker.getInstance().accept(email);
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        try {
            payment.finallyGenerateOutput(outputGenerator);
        } catch (NullPointerException e) {
            return;
        }
    }
}
