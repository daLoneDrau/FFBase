package com.dalonedrow.module.ff.rpg;

/**
 * 
 * @author 588648
 *
 */
public enum FFCommand {
    ATTACK(7),
    BRIBE(13),
    CLIMB(4),
    EAST(3),
    EAT(16),
    GIVE(6),
    INTIMIDATE(14),
    INVENTORY(19),
    NORTH(0),
    OPEN(8),
    SEARCH(17),
    SIT(11),
    SMASH(9),
    SOUTH(1),
    STEP(12),
    TAKE(10),
    TALK(6),
    THROW(15),
    USE(18),
    WEST(2);
    /** the sort order. */
    private int sortOrder;
    /**
     * Creates a new instance of {@link FFCommand}.
     * @param val the sort order
     */
    FFCommand(final int val) {
        sortOrder = val;
    }
    /**
     * Gets the event name associated with the command.
     * @return {@link String}
     */
    public String getEventName() {
        return toString().toLowerCase().replace(toString().charAt(0),
                Character.toUpperCase(toString().charAt(0)));
    }
    /**
     * Gets the sort order.
     * @return {@link int}
     */
    public int getSortOrder() {
        return sortOrder;
    }
}
