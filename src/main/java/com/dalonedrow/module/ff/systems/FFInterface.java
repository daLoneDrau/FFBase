package com.dalonedrow.module.ff.systems;

import com.dalonedrow.rpg.base.flyweights.BaseInteractiveObject;

public class FFInterface {
    private static FFInterface instance;
    public static int INVENTORY = 2;
    public static int WELCOME = 1;
    /**
     * @return the instance
     */
    public static FFInterface getInstance() {
        if (instance == null) {
            instance = new FFInterface();
        }
        return instance;
    }
    private int flags;
    /**
     *
     */
    private FFInterface() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * Adds a flag.
     * @param flag the flag
     */
    public void addFlag(final int flag) {
        flags |= flag;
    }
    /** Clears all flags that were set. */
    public void clearTypeFlags() {
        flags = 0;
    }
    /**
     * Determines if the {@link BaseInteractiveObject} has a specific flag.
     * @param flag the flag
     * @return true if the {@link BaseInteractiveObject} has the flag; false
     *         otherwise
     */
    public final boolean hasFlag(final long flag) {
        return (flags & flag) == flag;
    }
    /**
     * Removes a flag.
     * @param flag the flag
     */
    public final void removeFlag(final int flag) {
        flags &= ~flag;
    }
}
