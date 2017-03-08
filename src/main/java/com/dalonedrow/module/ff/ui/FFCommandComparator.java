package com.dalonedrow.module.ff.ui;

import java.util.Comparator;

import com.dalonedrow.module.ff.rpg.FFCommand;

/**
 * @author 588648
 */
public final class FFCommandComparator implements Comparator<FFCommand> {
    /** the one and only instance of the <code>WelcomeScreen</code> class. */
    private static FFCommandComparator instance;
    /**
     * Gives access to the singleton instance of {@link CombatScreen}.
     * @return {@link CombatScreen}
     */
    public static FFCommandComparator getInstance() {
        if (FFCommandComparator.instance == null) {
            FFCommandComparator.instance = new FFCommandComparator();
        }
        return FFCommandComparator.instance;
    }
    /** Hidden constructor. */
    private FFCommandComparator() {
        super();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FFCommand o1, final FFCommand o2) {
        int compares = 0;
        if (o1.getSortOrder() < o2.getSortOrder()) {
            compares = -1;
        } else if (o1.getSortOrder() < o2.getSortOrder()) {
            compares = 1;
        }
        return compares;
    }
}
