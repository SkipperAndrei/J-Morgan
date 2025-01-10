package org.poo.command;

import lombok.Getter;

/**
 * This enum is a reference to the signals sent during the program by the commands
 */
@Getter
public enum CommandConstants {

    SUCCESS(0),
    CLASSIC_ACC(-1),
    NOT_FOUND(-2),
    UNKNOWN_CURRENCY(-3),
    FROZEN_CARD(-4),
    INSUFFICIENT_FUNDS(-5),
    UNKNOWN_CARD(-6),
    SAVING_ACC(-7),
    MINIMUM_AGE(-8),
    INFERIOR_PLAN(-9),
    EQUAL_PLAN(-10),
    DELETED_CARD(-11),
    REJECTED_SPLIT(-12),
    DEPOSIT_LIMIT(-13),
    NO_PERMISSION(-14),
    COMMERCIANT_REC(1),
    USER_REC(2),
    ADULT_AGE(21),
    MAX_DIFF(30);

    private final int value;

    CommandConstants(final int value) {
        this.value = value;
    }

}
