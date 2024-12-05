package org.poo.command;

import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;


public class DeleteCard implements Command {

    private String email;
    private String cardNumber;
    private int timestamp;

    public DeleteCard(CommandInput command) {
        email = command.getEmail();
        cardNumber = command.getCardNumber();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        //TODO
        return;
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        //TODO
        return;
    }
}
