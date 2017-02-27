package com.dalonedrow.module.ff.rpg;

import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.ScriptConstants;
import com.dalonedrow.rpg.base.flyweights.Scriptable;

/**
 * @author drau
 */
public abstract class FFScriptable extends Scriptable<FFInteractiveObject> {
    /**
     * Creates a new instance of {@link FFScriptable}.
     * @param io the IO associated with this script
     */
    public FFScriptable(final FFInteractiveObject io) {
        super(io);
    }
    /**
     * Bashing a door.
     * @return {link int}
     * @throws RPGException if an error occurs
     */
    public int onBashDoor() {
        return ScriptConstants.ACCEPT;
    }
    /**
     * Entering the dungeon.
     * @return {link int}
     * @throws RPGException if an error occurs
     */
    public int onEnterDungeon() {
        return ScriptConstants.ACCEPT;
    }
    /**
     * Entering a room.
     * @return {link int}
     * @throws RPGException if an error occurs
     */
    public int onEnterRoom() {
        return ScriptConstants.ACCEPT;
    }
    /**
     * Exiting a room.
     * @return {link int}
     * @throws RPGException if an error occurs
     */
    public int onExitRoom() {
        return ScriptConstants.ACCEPT;
    }
    /**
     * IO was hit.
     * @return {link int}
     * @throws RPGException if an error occurs
     */
    @Override
    public int onHit() {
        return ScriptConstants.ACCEPT;
    }
}
