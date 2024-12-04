package org.poo.card;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.utils.Utils;

public class Card {

    public String cardNumber;
    public StringBuilder status;

    public Card() {
        cardNumber = Utils.generateCardNumber();
        status = new StringBuilder("active");
    }

    public ObjectNode cardToJson(ObjectMapper mapper) {

        ObjectNode cardNode = mapper.createObjectNode();
        cardNode.put("cardNumber", cardNumber);
        cardNode.put("status", status.toString());
        return cardNode;
    }
}
