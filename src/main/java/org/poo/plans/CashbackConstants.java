package org.poo.plans;

import lombok.Getter;

@Getter
public enum CashbackConstants {

    STD_ONE_HUNDRED_PERCENTAGE(0.001),
    STD_THREE_HUNDRED_PERCENTAGE(0.002),
    STD_FIVE_HUNDRED_PERCENTAGE(0.0025),
    SILVER_ONE_HUNDRED_PERCENTAGE(0.003),
    SILVER_THREE_HUNDRED_PERCENTAGE(0.004),
    SILVER_FIVE_HUNDRED_PERCENTAGE(0.005),
    GOLD_ONE_HUNDRED_PERCENTAGE(0.005),
    GOLD_THREE_HUNDRED_PERCENTAGE(0.0055),
    GOLD_FIVE_HUNDRED_PERCENTAGE(0.007);

    private final double value;

    CashbackConstants(final double value) {
        this.value = value;
    }
}
