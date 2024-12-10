package org.poo.card;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.utils.Utils;
import lombok.Data;

@Data
public class Card {

    private String cardNumber;
    private StringBuilder status;

    public Card() {
        cardNumber = Utils.generateCardNumber();
        status = new StringBuilder("active");
    }

    public void customSetStatus(String status) {
        this.status.setLength(0);
        this.status.append(status);
    }

    public void changeCardStatus(Account acc) {

        if (acc.getBalance() - acc.getMinimumBalance() <= 30) {
            customSetStatus("warning");
        }

        if (acc.getBalance() <= acc.getMinimumBalance()) {
            customSetStatus("frozen");
        }
    }

    public ObjectNode cardToJson(ObjectMapper mapper) {

        ObjectNode cardNode = mapper.createObjectNode();
        cardNode.put("cardNumber", cardNumber);
        cardNode.put("status", status.toString());
        return cardNode;
    }
}
