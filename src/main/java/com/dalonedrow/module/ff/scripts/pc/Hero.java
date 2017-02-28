package com.dalonedrow.module.ff.scripts.pc;

import com.dalonedrow.module.ff.graph.FFRoomNode;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.module.ff.ui.GameScreen;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.rpg.base.constants.ScriptConsts;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.ScriptConstants;
import com.dalonedrow.rpg.base.flyweights.SpeechParameters;
import com.dalonedrow.rpg.base.systems.Script;

public class Hero extends FFScriptable {
    public Hero(FFInteractiveObject io) {
        super(io);
        // TODO Auto-generated constructor stub
    }
    private String getLocalVarCombatMessage() throws RPGException {
        return super.getLocalStringVariableValue("combat_message");
    }
    /**
     * Gets the amount of 'OUCH' damage that occurred.
     * @return {@link float}
     * @throws RPGException if an error occurs
     */
    private float getLocalVarOuch() throws RPGException {
        return super.getLocalFloatVariableValue("OUCH");
    }
    /**
     * Gets the amount of 'SUMMONED OUCH' damage that occurred.
     * @return {@link float}
     * @throws RPGException if an error occurs
     */
    private float getLocalVarSummonedOuch() throws RPGException {
        return super.getLocalFloatVariableValue("SUMMONED_OUCH");
    }
    /**
     * Initializes all local variables.
     * @throws RPGException if an error occurs
     */
    private void initLocalVars() throws RPGException {
        setLocalVarCombatMessage("");
    }
    public int onEnterRoom12() throws RPGException {
        FFInteractiveObject io = FFWebServiceClient.getInstance().loadNPC("DOOR_12");
        return ScriptConstants.ACCEPT;
    }
    public int onEast() throws RPGException {
        // get room occupied
        FFRoomNode room = FFWorldMap.getInstance().getPlayerRoom();
        switch (room.getId()) {
        case 1:
            goEastRoom1();
            break;
        }
        return ScriptConstants.ACCEPT;
    }
    private void goEastRoom1() throws RPGException {
        // put hero in room 12
        super.getIO().setPosition(FFWorldMap.getInstance().getRoom(
                12).getMainNode().getLocation());
        // add action text
        GameScreen.getInstance().addMessage(
                FFWebServiceClient.getInstance().loadText("1_EAST"));
    }
    @Override
    public int onInit() throws RPGException {
        initLocalVars();
        return super.onInit();
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onOuch()
     */
    @Override
    public int onOuch() throws RPGException {
        System.out.println("on ouch");
        ouchStart();
        return ScriptConsts.ACCEPT;
    }
    /**
     * Starts the ouch event.
     * @throws PooledException if an error occurs
     * @throws RPGException if an error occurs
     */
    private void ouchStart() throws RPGException {
        System.out.print("OUCH ");
        float ouchDmg = getLocalVarSummonedOuch() + getLocalVarOuch();
        System.out.print(ouchDmg);
        // speak combat message first
        if (getLocalVarCombatMessage().length() > 0) {
            Script.getInstance().speak(super.getIO(),
                    new SpeechParameters("",
                            String.format(getLocalVarCombatMessage(),
                                    (int) ouchDmg)));
        }
    }
    private void setLocalVarCombatMessage(final String val)
            throws RPGException {
        super.setLocalVariable("combat_message", val);
    }
}
