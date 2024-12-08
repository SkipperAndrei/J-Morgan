package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.card.Card;
import org.poo.card.OneTimeCard;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class CreateOneTimeCard implements Command {

    private final static int SUCCESS = 0;
    private final static int FAILURE = 1;

    private int timestamp;
    private String account;
    private String email;
    private String cardNumber;
    private int actionCode = SUCCESS;

    public CreateOneTimeCard(CommandInput command) {
        account = command.getAccount();
        email = command.getEmail();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(UserDatabase userDatabase) {

        if (userDatabase.getEntry(email).getUserAccounts().containsKey(account)) {
            Card card = new OneTimeCard();
            cardNumber = card.getCardNumber();
            userDatabase.getEntry(email).addCard(account, card);
            return;
        }

        actionCode = FAILURE;
    }

    @Override
    public void generateOutput(OutputGenerator outputGenerator) {

        ObjectNode oneTimeNode = outputGenerator.getMapper().createObjectNode();
        oneTimeNode.put("timestamp", timestamp);

        if (actionCode == SUCCESS) {
            oneTimeNode.put("description", "New card created");
            oneTimeNode.put("card", cardNumber);
            oneTimeNode.put("cardHolder", email);
            oneTimeNode.put("account", account);
        } else {
            oneTimeNode.put("description", "Card not created");
        }

        outputGenerator.getUserDatabase().getEntry(email).addTransaction(oneTimeNode);
    }
}
