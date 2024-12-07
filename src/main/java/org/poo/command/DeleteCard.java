package org.poo.command;

import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;


public class DeleteCard implements Command {

    private String email;
    private String cardNumber;
    private int timestamp;
    private boolean found = false;

    public DeleteCard(CommandInput command) {
        email = command.getEmail();
        cardNumber = command.getCardNumber();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {
        try {
            for (Account ac : userDatabase.getEntry(email).getUserAccounts().values()) {

                Card card = ac.getCards().get(cardNumber);

                if (card != null) {
                    userDatabase.getDatabase().get(email).getUserAccounts().
                                get(ac.getIBAN()).getCards().remove(card.getCardNumber());
                }
            }
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {
        return;
    }
}
