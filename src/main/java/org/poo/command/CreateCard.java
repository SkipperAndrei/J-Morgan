package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class CreateCard implements Command {

    private static final int SUCCESS = 0;
    private static final int FAILURE = -1;

    private String cardNumber;
    private String cardHolder;
    private String account;
    private int timestamp;
    private int actionCode = SUCCESS;

    public CreateCard(final CommandInput command) {
        cardHolder = command.getEmail();
        account = command.getAccount();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        if (userDatabase.getUserEntry(cardHolder).getUserAccounts().containsKey(account)) {
            Card card = new Card();
            cardNumber = card.getCardNumber();
            userDatabase.getUserEntry(cardHolder).addCard(account, card);
            return;
        }

        actionCode = FAILURE;
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        ObjectNode createCardNode = outputGenerator.getMapper().createObjectNode();
        createCardNode.put("timestamp", timestamp);

        switch (actionCode) {
            case SUCCESS:
                createCardNode.put("description", "New card created");
                createCardNode.put("card", cardNumber);
                createCardNode.put("cardHolder", cardHolder);
                createCardNode.put("account", account);
                break;

            case FAILURE:
                createCardNode.put("description", "Couldn't create card");
                break;

            default:
                break;
        }


        outputGenerator.getUserDatabase().getUserEntry(cardHolder).addTransaction(createCardNode);
        Account acc = outputGenerator.getUserDatabase().getUserEntry(cardHolder).
                                                getUserAccounts().get(account);
        outputGenerator.tryToAddTransaction(acc, createCardNode);
    }

}
