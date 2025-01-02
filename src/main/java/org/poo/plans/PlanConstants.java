package org.poo.plans;

import lombok.Getter;

@Getter
public enum PlanConstants {

    SILVER_THRESHOLD(500),
    SCALING_FACTOR(1000);

    private final int value;

    PlanConstants(final int value) {
        this.value = value;
    }
}
