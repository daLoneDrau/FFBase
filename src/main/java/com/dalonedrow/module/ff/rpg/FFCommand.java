package com.dalonedrow.module.ff.rpg;

public enum FFCommand {
    ATTACK(6),
    BRIBE(12),
    EAST(3),
    EAT(15),
    GIVE(5),
    INTIMIDATE(13),
    INVENTORY(18),
    NORTH(0),
    OPEN(7),
    SEARCH(16),
    SIT(10),
    SMASH(8),
    SOUTH(1),
    STEP(11),
    TAKE(9),
    TALK(4),
    THROW(14),
    USE(17),
    WEST(2);
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
