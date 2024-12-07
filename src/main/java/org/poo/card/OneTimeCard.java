package org.poo.card;

import lombok.Data;

@Data
public class OneTimeCard extends Card {

    private int expired;

    public OneTimeCard() {
        super();
        expired = 0;
    }

}
