package org.poo.card;

import lombok.Data;

@Data
public class OneTimeCard extends Card {

    private boolean expired;

    public OneTimeCard() {
        super();
        expired = false;
    }

}
