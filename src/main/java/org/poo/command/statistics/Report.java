package org.poo.command.statistics;

import org.poo.command.Command;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class Report implements Command {

    private int startTimestamp;
    private int endTimestamp;
    private String account;
    private int timestamp;
    private String email;

    public Report(final CommandInput command) {
        startTimestamp = command.getStartTimestamp();
        endTimestamp = command.getEndTimestamp();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {
        email = userDatabase.getMailEntry(account);
        return;
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        if (email == null) {
            outputGenerator.errorSetting(timestamp, "Account not found", "report");
        }

        outputGenerator.generateReport(startTimestamp, endTimestamp, email, account, timestamp);
    }
}
