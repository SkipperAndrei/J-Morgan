package org.poo.command.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.card.Card;
import org.poo.command.Command;
import org.poo.command.CommandConstants;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.utils.OutputGenerator;


public final class DeleteCard implements Command {

    private String email;
    private String cardNumber;
    private int timestamp;
    private String account;
    private boolean found = false;
    private CommandConstants actionCode = CommandConstants.SUCCESS;

    public DeleteCard(final CommandInput command) {
        email = command.getEmail();
        cardNumber = command.getCardNumber();
        timestamp = command.getTimestamp();
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {


        for (Account ac : userDatabase.getUserEntry(email).getUserAccounts().values()) {

            Card card = ac.getCards().get(cardNumber);

            if (card != null) {

                if (ac.getAccountType().equals("business")) {
                    boolean canDelete = ((BusinessAccount) ac).deleteCardCheck(card, email);

                    if (!canDelete) {
                        actionCode = CommandConstants.NO_PERMISSION;
                        return;
                    }
                }

                userDatabase.getDatabase().get(email).getUserAccounts().
                                get(ac.getIban()).getCards().remove(card.getCardNumber());
                account = ac.getIban();
                found = true;
            }
        }


    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        if (actionCode.equals(CommandConstants.NO_PERMISSION)) {
            outputGenerator.errorSetting(timestamp,
                    "You are not authorized to make this transaction", "deleteCard");
            return;
        }

        ObjectNode deleteCardNode = outputGenerator.getMapper().createObjectNode();
        deleteCardNode.put("timestamp", timestamp);

        if (found) {
            deleteCardNode.put("description", "The card has been destroyed");
            deleteCardNode.put("card", cardNumber);
            deleteCardNode.put("cardHolder", email);
            deleteCardNode.put("account", account);
        } else {
            deleteCardNode.put("description", "Card not found");
        }

        outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(deleteCardNode);
        Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                        getUserAccounts().get(account);

        outputGenerator.tryToAddTransaction(acc, deleteCardNode);
    }
}
