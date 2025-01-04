package org.poo.plans;

import lombok.Getter;

@Getter
public enum PlanConstants {

    STD_TO_SILVER(100),
    SILVER_TO_GOLD(250),
    STD_TO_GOLD(350),
    SILVER_THRESHOLD(500),
    SCALING_FACTOR(1000);

    private final int value;

    PlanConstants(final int value) {
        this.value = value;
    }
}
