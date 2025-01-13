package org.poo.command.payments;

import org.poo.command.Command;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class RejectSplitPayment implements Command {

    private String email;
    private String type;
    private int timestamp;
    private SplitPayment payment;

    public RejectSplitPayment(final CommandInput command) {

        email = command.getEmail();
        type = command.getType();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        payment = SplitTracker.getInstance().reject(email);
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        try {

            payment.finallyGenerateOutput(outputGenerator);
        } catch (NullPointerException e) {
            outputGenerator.errorSetting(timestamp, "User not found", "rejectSplitPayment");
        }

    }
}
