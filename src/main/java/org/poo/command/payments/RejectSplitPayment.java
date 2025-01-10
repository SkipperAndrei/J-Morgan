package org.poo.command.payments;

import org.poo.command.Command;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public class RejectSplitPayment implements Command {

    private String email;
    private String type;
    private int timestamp;
    private SplitPayment payment;

    public RejectSplitPayment(CommandInput command) {

        email = command.getEmail();
        type = command.getType();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        payment = SplitTracker.getInstance().reject(email);
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
//
//        if (payment == null) {
//            outputGenerator.errorSetting(timestamp, "User not found", "rejectSplitPayment");
//            return;
//        }
//
//        payment.finallyGenerateOutput(outputGenerator);
//
        try {

            payment.finallyGenerateOutput(outputGenerator);
        } catch (NullPointerException e) {
            outputGenerator.errorSetting(timestamp, "User not found", "rejectSplitPayment");
        }
    }
}
