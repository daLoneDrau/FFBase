package com.dalonedrow.module.ff.systems;

import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFNpc;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.CombatUtility;
import com.dalonedrow.rpg.base.systems.Script;
import com.dalonedrow.utils.ArrayUtilities;

public final class Combat extends CombatUtility<FFInteractiveObject> {
    /** the list of enemies. */
    private FFInteractiveObject[] enemies = new FFInteractiveObject[0];
    public void addEnemy(final FFInteractiveObject io) throws RPGException {
        if (io == null) {
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "Cannot add null enemy");
        }
        if (!io.hasIOFlag(IoGlobals.IO_03_NPC)) {
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "Enemy must be NPC");
        }
        enemies = ArrayUtilities.getInstance().extendArray(io, enemies);
    }
    private void checkDead() throws RPGException {
        for (int i = this.enemies.length - 1; i >= 0; i--) {
            FFNpc enemy = enemies[i].getNPCData();
            enemy.computeFullStats();
            if (enemy.IsDeadNPC()) {
                Script.getInstance().sendIOScriptEvent(target, msg, params, eventname)
            }
        }
    }
    private void removeEnemy(final FFInteractiveObject io) throws RPGException {
        int index = -1;
        for (int i = this.enemies.length - 1; i >= 0; i--) {
            if (enemies[i].equals(io)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "Cannot remove enemy " + io.getRefId());
        }
        enemies = ArrayUtilities.getInstance().removeIndex(index, enemies);
    }
}
