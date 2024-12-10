package org.poo.command;

import org.poo.account.Account;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class Report implements Command {

    private int startTimestamp;
    private int endTimestamp;
    private String account;
    private int timestamp;
    private String email;

    public Report(CommandInput command) {
        startTimestamp = command.getStartTimestamp();
        endTimestamp = command.getEndTimestamp();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        // System.out.println("Sunt aici");
        email = userDatabase.getMailEntry(account);
        return;
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        if (email == null) {
            outputGenerator.errorSetting(timestamp, "Account not found", "report");
        }
        outputGenerator.generateReport(startTimestamp, endTimestamp, email, account, timestamp);
    }
}
