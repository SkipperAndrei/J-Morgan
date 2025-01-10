package org.poo.command.payments;

import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public class AcceptSplitPayment implements Command {


    private String email;
    private String type;
    private int timestamp;
    private SplitPayment payment;

    public AcceptSplitPayment(CommandInput command) {

        email = command.getEmail();
        type = command.getSplitPaymentType();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        payment = SplitTracker.getInstance().accept(email);
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        if (payment.getActionCode().equals(CommandConstants.NOT_FOUND)) {
            outputGenerator.errorSetting(timestamp, "User not found", "acceptSplitPayment");
            return;
        }

        try {
            payment.finallyGenerateOutput(outputGenerator);
        } catch (NullPointerException e) {
            return;
        }
    }
}
