package org.poo.card;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.utils.Utils;
import lombok.Data;

/**
 * This class will contain card information
 */
@Data
public class Card {

    private String cardOwner;
    private String cardNumber;
    private StringBuilder status;

    public Card() {
        cardNumber = Utils.generateCardNumber();
        status = new StringBuilder("active");
    }

    /**
     * Change the status of the card
     * @param stat Can only be "active" or "frozen"
     */
    public void customSetStatus(final String stat) {
        status.setLength(0);
        status.append(stat);
    }

    /**
     * Function to check if the status should be changed
     * If the status is "frozen", it can't be changed
     * @param acc Account that the card is associated with
     */
    public void changeCardStatus(final Account acc) {

        if (acc.getBalance() < acc.getMinimumBalance()) {
            customSetStatus("frozen");
        }
    }

    /**
     * Function that maps the contents of a card to a JSON node
     * @param mapper The object mapper
     * @return The mapped JSON node
     */
    public ObjectNode cardToJson(final ObjectMapper mapper) {

        ObjectNode cardNode = mapper.createObjectNode();
        cardNode.put("cardNumber", cardNumber);
        cardNode.put("status", status.toString());
        return cardNode;
    }
}
