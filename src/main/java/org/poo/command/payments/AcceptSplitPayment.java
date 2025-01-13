package org.poo.command.payments;

import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class AcceptSplitPayment implements Command {

    private String email;
    private String type;
    private int timestamp;
    private SplitPayment payment;

    public AcceptSplitPayment(final CommandInput command) {

        email = command.getEmail();
        type = command.getSplitPaymentType();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        payment = SplitTracker.getInstance().accept(email, type);
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

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
