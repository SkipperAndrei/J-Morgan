package org.poo.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.utils.Utils;

/**
 * This class extends the Card class as it implements the logic
 * of the One-Time Pay
 */
@Data
public class OneTimeCard extends Card {

    private int expired;

    public OneTimeCard() {
        super();
        expired = 0;
    }

    /**
     * This function maps the change of the card number after one payment in a JSON node
     * @param timestamp
     * @param description
     * @param change This marks if the payment happened
     * @return The mapped JSON node
     */
    public ObjectNode updateCardNumber(final int timestamp, final String description,
                                       final boolean change) {

        ObjectNode cardNode = new ObjectMapper().createObjectNode();

        cardNode.put("timestamp", timestamp);
        cardNode.put("description", description);
        cardNode.put("card", super.getCardNumber());

        if (change) {
            // if payment happened, change it's card number
            super.setCardNumber(Utils.generateCardNumber());
        }

        return cardNode;
    }

}
