package com.dalonedrow.module.ff.rpg;

public enum FFCommand {
    NORTH(0),
    SOUTH(1),
    WEST(2),
    EAST(3),
    TALK(4),
    GIVE(5),
    ATTACK(6),
    OPEN(7),
    SMASH(8),
    TAKE(9),
    SIT(10),
    STEP(11),
    BRIBE(12),
    INTIMIDATE(13),
    THROW(14),
    EAT(15),
    SEARCH(16),
    USE(17),
    INVENTORY(18);
    private int sortOrder;
    /**
     * Creates a new instance of {@link FFCommand}.
     * @param val the sort order
     */
    FFCommand(final int val) {
        sortOrder = val;
    }
    /**
     * Gets the sort order.
     * @return {@link int}
     */
    public int getSortOrder() {
        return sortOrder;
    }
}
