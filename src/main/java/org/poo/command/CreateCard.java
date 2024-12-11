package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class CreateCard implements Command {

    private String cardNumber;
    private String cardHolder;
    private String account;
    private int timestamp;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

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

        actionCode = CommandConstants.NOT_FOUND;
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

            case NOT_FOUND:
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
