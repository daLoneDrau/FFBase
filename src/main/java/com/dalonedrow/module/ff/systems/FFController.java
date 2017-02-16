package com.dalonedrow.module.ff.systems;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.constants.FFEquipmentElements;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.rpg.base.constants.EquipmentGlobals;
import com.dalonedrow.rpg.base.flyweights.RPGException;

public final class FFController extends ProjectConstants<FFInteractiveObject> {
    /* (non-Javadoc)
     * @see com.dalonedrow.engine.systems.base.ProjectConstants#isGameOver()
     */
    @Override
    public boolean isGameOver() {
        return false;
    }
    private boolean menusOn;
    /** the player IO's id. */
    private int playerId = -1;
    /**
     *
     */
    public FFController() {
        super.setInstance(this);
        menusOn = true;
    }
    @Override
    public int getConsoleHeight() {
        return 20;
    }
    @Override
    public int getConsoleWidth() {
        return 100;
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
        return playerId;
    }
    public FFInteractiveObject getPlayerIO() throws RPGException {
        return (FFInteractiveObject) Interactive.getInstance().getIO(
                playerId);
    }
    /**
     * @return the menusOn
     */
    public boolean isMenusOn() {
        return menusOn;
    }
    /**
     * @param menusOn the menusOn to set
     */
    public void setMenusOn(boolean menusOn) {
        this.menusOn = menusOn;
    }
    /**
     * @param val the value to set
     */
    void setPlayer(int val) {
        playerId = val;
    }
    @Override
    public void update() {
        // TODO Auto-generated method stub

    }
}
