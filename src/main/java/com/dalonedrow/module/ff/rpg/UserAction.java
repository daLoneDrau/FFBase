package com.dalonedrow.module.ff.rpg;

/**
 * @author drau
 */
public enum UserAction {
    /** BASH. */
    ATT(32, "ATTACK"),
    /** BASH. */
    BAS(16, "BASH"),
    /** EAST. */
    EAS(1, "EAST"),
    /** NORTH. */
    NOR(2, "NORTH"),
    /** SOUTH. */
    SOU(4, "SOUTH"),
    /** WEST. */
    WES(8, "WEST");
    /**
     * /** the name.
     */
    private final String name;
    /** the numeric value. */
    private final int value;
    /**
     * Creates a new instance of {@link UserAction}.
     * @param val the numeric value
     * @param n the name
     */
    UserAction(final int val, final String n) {
        value = val;
        name = n;
    }
    /**
     * Gets the name.
     * @return {@link String}
     */
    public String getName() {
        return name;
    }
    /**
     * Gets the numeric value.
     * @return {@link int}
     */
    public int getValue() {
        return value;
    }
}
