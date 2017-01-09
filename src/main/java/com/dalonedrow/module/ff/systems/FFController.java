package com.dalonedrow.module.ff.systems;

import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.constants.FFEquipmentElements;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.rpg.base.constants.EquipmentGlobals;
import com.dalonedrow.rpg.base.flyweights.RPGException;

public class FFController extends ProjectConstants<FFInteractiveObject> {    
    /**
     * 
     */
    public FFController() {
        super.setInstance(this);
    }

    @Override
    public void update() {
        // TODO Auto-generated method stub
        
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
    private int playerId;
    @Override
    public int getPlayer() throws RPGException {
        // TODO Auto-generated method stub
        return playerId;
    }
    public FFInteractiveObject getPlayerIO() throws RPGException {
        return (FFInteractiveObject) FFInteractive.getInstance().getIO(
                playerId);
    }

    @Override
    public int getNumberEquipmentElements() {
        return FFEquipmentElements.getNumberOfValues();
    }

}
