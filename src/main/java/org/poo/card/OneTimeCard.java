package org.poo.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.utils.Utils;

@Data
public class OneTimeCard extends Card {

    private int expired;

    public OneTimeCard() {
        super();
        expired = 0;
    }

    public ObjectNode updateCardNumber(final int timestamp, final String description, final boolean change) {

        ObjectNode cardNode = new ObjectMapper().createObjectNode();

        cardNode.put("timestamp", timestamp);
        cardNode.put("description", description);
        cardNode.put("card", super.getCardNumber());

        if (change) {
            super.setCardNumber(Utils.generateCardNumber());
        }

        return cardNode;
    }

}
