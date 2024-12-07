package org.poo.command;

import org.poo.card.Card;
import org.poo.card.OneTimeCard;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class CreateOneTimeCard implements Command {

    private String account;
    private String email;
    private boolean error = false;

    public CreateOneTimeCard(CommandInput command) {
        account = command.getAccount();
        email = command.getEmail();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        Card oneTimeCard = new OneTimeCard();

        userDatabase.getEntry(email).addCard(account, oneTimeCard);
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        return;
    }
}
