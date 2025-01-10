package org.poo.utils;

import lombok.Getter;

@Getter
public enum DiscountTracker {

    NOT_ELIGIBLE(0),
    ELIGIBLE(1),
    CASHED_IN(2),
    CLOTHES_THRESHOLD(5),
    TECH_THRESHOLD(10),
    ONE_HUNDRED_THRESHOLD(100),
    THREE_HUNDRED_THRESHOLD(300),
    FIVE_HUNDRED_THRESHOLD(500);

    private final int value;

    private DiscountTracker(final int value) {
        this.value = value;
    }
}
