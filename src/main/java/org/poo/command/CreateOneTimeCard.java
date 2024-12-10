package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.card.OneTimeCard;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;

public class CreateOneTimeCard implements Command {

    private static final int SUCCESS = 0;
    private static final int FAILURE = 1;

    private int timestamp;
    private String account;
    private String email;
    private String cardNumber;
    private int actionCode = SUCCESS;

    public CreateOneTimeCard(final CommandInput command) {
        account = command.getAccount();
        email = command.getEmail();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        if (userDatabase.getUserEntry(email).getUserAccounts().containsKey(account)) {
            Card card = new OneTimeCard();
            cardNumber = card.getCardNumber();
            userDatabase.getUserEntry(email).addCard(account, card);
            return;
        }

        actionCode = FAILURE;
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

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

        outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(oneTimeNode);
        Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                        getUserAccounts().get(account);

        outputGenerator.tryToAddTransaction(acc, oneTimeNode);
    }
}
