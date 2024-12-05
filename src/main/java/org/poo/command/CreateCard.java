package org.poo.command;

import org.poo.card.Card;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class CreateCard implements Command {

    private String cardNumber;
    private String cardHolder;
    private String account;
    private String description;
    private int timestamp;

    public CreateCard(CommandInput command) {
        // cardNumber = command.getCardNumber();
        cardHolder = command.getEmail();
        account = command.getAccount();
        description = command.getDescription();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        Card card = new Card();
        //TODO

        userDatabase.getEntry(cardHolder).addCard(account, card);
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator, int timestamp) {
        return;
    }

    // functie pt Tranzactii
}
