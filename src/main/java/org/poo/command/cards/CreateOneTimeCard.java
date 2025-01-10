package org.poo.command.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.card.OneTimeCard;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;

public final class CreateOneTimeCard implements Command {

    private int timestamp;
    private String account;
    private String email;
    private String cardNumber;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public CreateOneTimeCard(final CommandInput command) {
        account = command.getAccount();
        email = command.getEmail();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        if (userDatabase.getUserEntry(email).getUserAccounts().containsKey(account)) {
            Card card = new OneTimeCard();
            card.setCardOwner(email);
            cardNumber = card.getCardNumber();
            userDatabase.getUserEntry(email).addCard(account, card);
            return;
        }

        actionCode = CommandConstants.NOT_FOUND;
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        ObjectNode oneTimeNode = outputGenerator.getMapper().createObjectNode();
        oneTimeNode.put("timestamp", timestamp);

        if (actionCode == CommandConstants.SUCCESS) {
            oneTimeNode.put("description", "New card created");
            oneTimeNode.put("card", cardNumber);
            oneTimeNode.put("cardHolder", email);
            oneTimeNode.put("account", account);
        } else {
            oneTimeNode.put("description", "Card not created");
        }

        outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(oneTimeNode);
        Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                        getUserAccounts().get(account);

        outputGenerator.tryToAddTransaction(acc, oneTimeNode);
    }
}
