package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;


public class DeleteCard implements Command {

    private String email;
    private String cardNumber;
    private int timestamp;
    private String account;
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
                    account = ac.getIBAN();
                    found = true;
                }
            }
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        ObjectNode deleteCardNode = outputGenerator.getMapper().createObjectNode();
        deleteCardNode.put("timestamp", timestamp);
        if (found) {
            deleteCardNode.put("description", "The card has been destroyed");
            deleteCardNode.put("card", cardNumber);
            deleteCardNode.put("cardHolder", email);
            deleteCardNode.put("account", account);
        } else {
            deleteCardNode.put("description", "The card has not been destroyed");
        }
        outputGenerator.getUserDatabase().getEntry(email).addTransaction(deleteCardNode);
    }
}
