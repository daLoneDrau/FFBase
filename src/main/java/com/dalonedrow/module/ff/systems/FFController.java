package com.dalonedrow.module.ff.systems;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.constants.FFEquipmentElements;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.rpg.base.constants.EquipmentGlobals;
import com.dalonedrow.rpg.base.flyweights.RPGException;

public class FFController extends ProjectConstants<FFInteractiveObject> {
    private int playerId;
    /**
     *
     */
    public FFController() {
        super.setInstance(this);
    }
    @Override
    public int getDamageElementIndex() {
        return FFEquipmentElements.valueOf("ELEMENT_DAMAGE").getIndex();
    }
    @Override
    public int getMaxEquipped() {
        // TODO Auto-generated method stub
        return EquipmentGlobals.MAX_EQUIPPED;
    }
    @Override
    public int getMaxSpells() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public int getNumberEquipmentElements() {
        return FFEquipmentElements.getNumberOfValues();
    }
    @Override
    public int getPlayer() throws RPGException {
        // TODO Auto-generated method stub
        return playerId;
    }
    public FFInteractiveObject getPlayerIO() throws RPGException {
        return (FFInteractiveObject) Interactive.getInstance().getIO(
                playerId);
    }
    @Override
    public void update() {
        // TODO Auto-generated method stub

    }
}
