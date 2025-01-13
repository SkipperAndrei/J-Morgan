package org.poo.plans;

import lombok.Getter;

@Getter
public enum PlanConstants {

    BIG_PAY_AUTO_UPGRADE(5),
    STD_TO_SILVER(100),
    SILVER_TO_GOLD(250),
    AUTO_UPGRADE_THRESHOLD(300),
    STD_TO_GOLD(350),
    SILVER_THRESHOLD(500),
    FIRST_THRESHOLD(100),
    SECOND_THRESHOLD(300),
    FINAL_THRESHOLD(500),
    SCALING_FACTOR(1000);


    private final int value;

    PlanConstants(final int value) {
        this.value = value;
    }
}
