package com.dalonedrow.module.ff.scripts.npcs;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFCharacter;
import com.dalonedrow.module.ff.rpg.FFCommand;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.module.ff.ui.GameScreen;
import com.dalonedrow.rpg.base.constants.Dice;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.ScriptConstants;
import com.dalonedrow.rpg.base.systems.Script;

/**
 * @author drau
 */
@SuppressWarnings({ "unchecked" })
public class Door extends FFScriptable {
    /** the id of door 12. */
    private static final int DOOR_12_ID = 12;
    /** the room to which door 12 leads. */
    private static final int DOOR_12_ROOM_DEST = 139;
    /** the room from which door 12 leads. */
    private static final int DOOR_12_ROOM_SRC = 12;
    /** the id of door 43. */
    private static final int DOOR_43_ID = 43;
    /** the room to which door 43 leads. */
    private static final int DOOR_43_ROOM_DEST = 82;
    /** door 12's location. */
    private static final SimpleVector2 V2_12 = new SimpleVector2(648, 1337);
    /** door 43's location. */
    private static final SimpleVector2 V2_43 = new SimpleVector2(635, 1332);
    /**
     * Creates a new instance of {@link OrcCleaverScript}.
     * @param io the IO associated with the script
     */
    public Door(final FFInteractiveObject io) {
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
        super.setLocalVariable("locked", 0);
        String name = new String(super.getIO().getNPCData().getName());
        int i = name.indexOf('_');
        if (i >= 0) {
            i = Integer.parseInt(name.substring(i + 1));
            switch (i) {
            case DOOR_12_ID:
                super.setLocalVariable("locked", 1);
                super.getIO().setPosition(V2_12);
                break;
            case DOOR_43_ID:
                super.getIO().setPosition(V2_43);
                break;
            default:
            }
        }
        name = null;
        return super.onInitEnd();
    }
    /**
     * On Open event.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onOpen() throws RPGException {
        if (super.getLocalIntVariableValue("locked") == 1) {
            GameScreen.getInstance().addMessage(
                    FFWebServiceClient.getInstance().loadText(
                            "open_locked_door"));
        } else {
            String name = new String(super.getIO().getNPCData().getName());
            int i = name.indexOf('_');
            if (i >= 0) {
                i = Integer.parseInt(name.substring(i + 1));
                switch (i) {
                case DOOR_43_ID:
                    openDoor43();
                    break;
                default:
                }
            }
        }
        return ScriptConstants.ACCEPT;
    }
    /**
     * On Smash event.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onSmash() throws RPGException {
        String name = new String(super.getIO().getNPCData().getName());
        int i = name.indexOf('_');
        if (i >= 0) {
            i = Integer.parseInt(name.substring(i + 1));
            switch (i) {
            case DOOR_12_ID:
                smashDoor12();
                break;
            case DOOR_43_ID:
                smashDoor43();
                break;
            default:
            }
        }
        return ScriptConstants.ACCEPT;
    }
    /**
     * Processes the action of opening Door 43.
     * @throws RPGException if an error occurs
     */
    private void openDoor43() throws RPGException {
        // load orc sentry 2
        FFInteractiveObject io = FFWebServiceClient.getInstance().loadNPC(
                "ORC_SENTRY_2");
        io.setScriptLoaded(true);
        // load box 1
        io = FFWebServiceClient.getInstance().loadItem("BOX_1");
        io.setScriptLoaded(true);
        // send player a travel event
        Script.getInstance().sendIOScriptEvent(
                ((FFController) FFController.getInstance()).getPlayerIO(),
                0, new Object[] { "travel_direction", "WEST" }, "Travel");
    }
    /**
     * Processes the action of Smashing Door 12.
     * @throws RPGException if an error occurs
     */
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
                    DOOR_12_ROOM_DEST).getMainNode().getLocation());
            FFWorldMap.getInstance().getRoom(DOOR_12_ROOM_SRC).setDisplayText(
                    FFWebServiceClient.getInstance().loadText("12_TERTIARY"));
            Interactive.getInstance().ARX_INTERACTIVE_DestroyIO(super.getIO());
        } else {
            GameScreen.getInstance().addMessage(
                    FFWebServiceClient.getInstance().loadText(
                            "smash_door_12_failure"));
        }
    }
    /**
     * Processes the action of Smashing Door 43.
     * @throws RPGException if an error occurs
     */
    private void smashDoor43() throws RPGException {
        // put player in room 82
        FFInteractiveObject playerIO =
                ((FFController) ProjectConstants.getInstance()).getPlayerIO();
        playerIO.setPosition(FFWorldMap.getInstance().getRoom(
                DOOR_43_ROOM_DEST).getMainNode().getLocation());
        // load orc sentry 2
        FFInteractiveObject io = FFWebServiceClient.getInstance().loadNPC(
                "ORC_SENTRY_2");
        io.setScriptLoaded(true);
        // set orc to hear everything
        ((Orc) io.getScript()).setLocalVarHearEverything(true);
        // set orc'saggression text
        ((Orc) io.getScript()).setLocalSpeechAggression(
                FFWebServiceClient.getInstance().loadText(
                        "orc_sentry_2_aggression"));
        // send Hear event to wake up the orc
        Script.getInstance().sendIOScriptEvent(
                io, ScriptConstants.SM_046_HEAR, null, null);
        // load box 1
        io = FFWebServiceClient.getInstance().loadItem("BOX_1");
        io.setScriptLoaded(true);
        io = null;
        // destroy the door
        Interactive.getInstance().ARX_INTERACTIVE_DestroyIO(super.getIO());
        // add western exit to room 43
        FFWorldMap.getInstance().getRoom(DOOR_43_ID).addCommand(FFCommand.WEST);
    }
}
