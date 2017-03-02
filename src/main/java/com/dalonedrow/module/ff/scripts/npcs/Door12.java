package com.dalonedrow.module.ff.scripts.npcs;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFCharacter;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.module.ff.ui.GameScreen;
import com.dalonedrow.rpg.base.constants.Dice;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.ScriptConstants;

/**
 * @author drau
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Door12 extends FFScriptable {
    /**
     * Creates a new instance of {@link OrcCleaverScript}.
     * @param io the IO associated with the script
     */
    public Door12(final FFInteractiveObject io) {
        super(io);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int onInit() throws RPGException {
        return super.onInit();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int onInitEnd() throws RPGException {
        String name = new String(super.getIO().getNPCData().getName());
        if ("DOOR_12".equalsIgnoreCase(name)) {
            super.getIO().setPosition(new SimpleVector2(648, 1337));
        }
        name = null;
        return super.onInitEnd();
    }
    /**
     * On Smash event.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onSmash() throws RPGException {
        String name = new String(super.getIO().getNPCData().getName());
        if ("DOOR_12".equalsIgnoreCase(name)) {
            smashDoor12();
        }
        name = null;
        return ScriptConstants.ACCEPT;
    }
    private void smashDoor12() throws RPGException {
        FFInteractiveObject playerIO =
                ((FFController) ProjectConstants.getInstance()).getPlayerIO();
        FFCharacter player = playerIO.getPCData();
        player.computeFullStats();
        if (player.getFullAttributeScore("SK") > Dice.TWO_D6.roll()) {
            GameScreen.getInstance().addMessage(
                    FFWebServiceClient.getInstance().loadText(
                            "smash_door_12_success"));
            player.ARX_DAMAGES_DamagePlayer(1, 0, super.getIO().getRefId());
            playerIO.setPosition(FFWorldMap.getInstance().getRoom(
                    139).getMainNode().getLocation());
            
            FFWorldMap.getInstance().getRoom(12).setDisplayText(
                    FFWebServiceClient.getInstance().loadText("12_TERTIARY"));
            Interactive.getInstance().ARX_INTERACTIVE_DestroyIO(super.getIO());
        } else {
            GameScreen.getInstance().addMessage(
                    FFWebServiceClient.getInstance().loadText(
                            "smash_door_12_failure"));
        }
    }
}
