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
    MAX_DIFF(30);

    private final int value;

    CommandConstants(final int value) {
        this.value = value;
    }

}
