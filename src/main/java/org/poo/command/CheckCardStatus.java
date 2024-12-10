package org.poo.command;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.database.UserDatabase;
import org.poo.fileio.CommandInput;
import org.poo.output.OutputGenerator;
import org.poo.user.User;

public class CheckCardStatus implements Command {

    private String cardNumber;
    private int timestamp;
    private String previousStatus;
    private String email;
    private String iban;
    private Card card = null;

    public CheckCardStatus(final CommandInput command) {
        cardNumber = command.getCardNumber();
        timestamp = command.getTimestamp();
    }

    public void checkStatus(final Account acc, final Card checkedCard) {
        previousStatus = checkedCard.getStatus().toString();
        checkedCard.changeCardStatus(acc);
    }

    @Override
    public void executeCommand(final UserDatabase userDatabase) {

        for (User user : userDatabase.getDatabase().values()) {

            for (Account acc : user.getUserAccounts().values()) {

                if (acc.getCards().containsKey(cardNumber)) {
                    iban = acc.getIban();
                    email = acc.getEmail();
                    card = acc.getCards().get(cardNumber);
                    checkStatus(acc, acc.getCards().get(cardNumber));
                }
            }
        }
    }

    @Override
    public void generateOutput(final OutputGenerator outputGenerator) {

        try {

            switch (card.getStatus().toString()) {

                case "warning" :

                    ObjectNode warningNode = outputGenerator.getMapper().createObjectNode();
                    warningNode.put("timestamp", timestamp);
                    warningNode.put("description", "You are warned, stop spending");

                    outputGenerator.getUserDatabase().getUserEntry(email).
                                    addTransaction(warningNode);

                    Account acc = outputGenerator.getUserDatabase().getUserEntry(email).
                                    getUserAccounts().get(iban);

                    outputGenerator.tryToAddTransaction(acc, warningNode);
                    return;

                case "frozen" :
                    ObjectNode frozNode = outputGenerator.getMapper().createObjectNode();
                    if (previousStatus.equals("frozen")) {
                        return;
                    }
                    frozNode.put("timestamp", timestamp);
                    frozNode.put("description", "You have reached the minimum amount of "
                                    + "funds, the card will be frozen");

                    outputGenerator.getUserDatabase().getUserEntry(email).addTransaction(frozNode);
                    Account frozenAccount = outputGenerator.getUserDatabase().getUserEntry(email).
                                            getUserAccounts().get(iban);
                    outputGenerator.tryToAddTransaction(frozenAccount, frozNode);
                    return;

                default :
                    return;
            }
        } catch (NullPointerException ex) {
            outputGenerator.errorSetting(timestamp, "Card not found", "checkCardStatus");
        }

    }
}
