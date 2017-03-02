package com.dalonedrow.module.ff.scripts.pc;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.module.ff.graph.FFRoomNode;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFCommand;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.module.ff.systems.Combat;
import com.dalonedrow.module.ff.ui.GameScreen;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.consoleui.TextProcessor;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.constants.ScriptConsts;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.ScriptConstants;
import com.dalonedrow.rpg.base.flyweights.SpeechParameters;
import com.dalonedrow.rpg.base.systems.Script;
import com.dalonedrow.rpg.graph.PhysicalGraphNode;
import com.dalonedrow.utils.ArrayUtilities;

@SuppressWarnings("unchecked")
public final class Hero extends FFScriptable {
    public Hero(FFInteractiveObject io) {
        super(io);
        // TODO Auto-generated constructor stub
    }
    /**
     * Checks that there is a traversable path between two rooms. If a path
     * exists but is blocked, then local variable "blocked_message" is set.
     * @param room1 the id of the first room
     * @param room2 the id of the second room
     * @return <tt>true</tt> if a traversable path exists; <tt>false</tt>
     *         otherwise
     * @throws RPGException if an error occurs
     */
    private boolean checkPath(final int room1, final PhysicalGraphNode node2)
            throws RPGException {
        setLocalVarBlockedMessage("");
        boolean pass = false;
        PhysicalGraphNode node1 =
                FFWorldMap.getInstance().getRoom(room1).getMainNode();
        pass = checkPath(node1, node2);
        node1 = null;
        return pass;
    }
    /**
     * Checks that there is a traversable path between two rooms. If a path
     * exists but is blocked, then local variable "blocked_message" is set.
     * @param room1 the id of the first room
     * @param room2 the id of the second room
     * @return <tt>true</tt> if a traversable path exists; <tt>false</tt>
     *         otherwise
     * @throws RPGException if an error occurs
     */
    private boolean checkPath(final PhysicalGraphNode node1, final PhysicalGraphNode node2)
            throws RPGException {
        setLocalVarBlockedMessage("");
        boolean pass = false;
        if (FFWorldMap.getInstance().hasPath(node1, node2)) {
            FFInteractiveObject[] ios =
                    FFWorldMap.getInstance().getIosAlongPath(node1, node2);
            boolean blocked = false;
            if (ios != null) {
                for (int i = ios.length - 1; i >= 0; i--) {
                    if (ios[i].equals(super.getIO())) {
                        continue;
                    }
                    setLocalVarBlockedMessage(
                            TextProcessor.getInstance().processText(
                                    (FFInteractiveObject) null,
                                    ios[i],
                                    (String) null,
                                    FFWebServiceClient.getInstance().loadText(
                                            "exit_blocked")));
                    blocked = true;
                }
            }
            ios = null;
            if (!blocked) {
                pass = true;
            }
        }
        return pass;
    }
    /**
     * Gets the eastern destination when traveling from the source room. If
     * there is no valid destination, 0 is returned.
     * @param source the id of the source room
     * @return {@link PhysicalGraphNode}
     * @throws RPGException if an error occurs
     */
    private PhysicalGraphNode getDestinationEast(final int source)
            throws RPGException {
        PhysicalGraphNode destination = null;
        switch (source) {
        case 1:
            destination = FFWorldMap.getInstance().getRoom(12).getMainNode();
            break;
        case 12:
            if (!FFWorldMap.getInstance().getRoom(139).isVisited()) {
            destination =
                    FFWorldMap.getInstance().getRoom(139).getNode(650, 1337);
            } else {
                destination =
                        FFWorldMap.getInstance().getRoom(139).getMainNode();
            }
            break;
        case 71:
            destination = FFWorldMap.getInstance().getRoom(1).getMainNode();
            break;
        default:
            break;
        }
        return destination;
    }
    /**
     * Gets the northern destination when traveling from the source room. If
     * there is no valid destination, 0 is returned.
     * @param source the id of the source room
     * @return {@link PhysicalGraphNode}
     * @throws RPGException if an error occurs
     */
    private PhysicalGraphNode getDestinationNorth(final int source)
            throws RPGException {
        PhysicalGraphNode destination = null;
        switch (source) {
        default:
            break;
        }
        return destination;
    }
    /**
     * Gets the southern destination when traveling from the source room. If
     * there is no valid destination, 0 is returned.
     * @param source the id of the source room
     * @return {@link PhysicalGraphNode}
     * @throws RPGException if an error occurs
     */
    private PhysicalGraphNode getDestinationSouth(final int source)
            throws RPGException {
        PhysicalGraphNode destination = null;
        switch (source) {
        case 1:
            setLocalVarBlockedMessage(
                    FFWebServiceClient.getInstance().loadText("1_SOUTH"));
            break;
        default:
            break;
        }
        return destination;
    }
    /**
     * Gets the eastern destination when traveling from the source room. If
     * there is no valid destination, 0 is returned.
     * @param source the id of the source room
     * @return {@link PhysicalGraphNode}
     * @throws RPGException if an error occurs
     */
    private PhysicalGraphNode getDestinationWest(final int source)
            throws RPGException {
        PhysicalGraphNode destination = null;
        switch (source) {
        case 1:
            destination = FFWorldMap.getInstance().getRoom(71).getMainNode();
            break;
        case 12:
            destination = FFWorldMap.getInstance().getRoom(1).getMainNode();
            break;
        case 139:
            final int x2 = 650, y = 1337;
            if (super.getIO().getPosition().equals(x2, y)) {
                destination =
                        FFWorldMap.getInstance().getRoom(12).getMainNode();
            }
            break;
        default:
            break;
        }
        return destination;
    }
    /**
     * Gets the value of the local variable "blocked_message".
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
    private String getLocalVarBlockedMessage() throws RPGException {
        return super.getLocalStringVariableValue("blocked_message");
    }
    /**
     * Gets the value of the local variable "combat_message".
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
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
     * Gets the value of the local variable "travel_direction".
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
    private String getLocalVarTravelDirection() throws RPGException {
        return super.getLocalStringVariableValue("travel_direction");
    }
    /**
     * Initializes all local variables.
     * @throws RPGException if an error occurs
     */
    private void initLocalVars() throws RPGException {
        setLocalVarBlockedMessage("");
        setLocalVarCombatMessage("");
        setLocalVarTravelDirection("");
    }
    /**
     * On IO Climb.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onClimb() throws RPGException {
        // get room occupied
        FFRoomNode room = FFWorldMap.getInstance().getPlayerRoom();
        int source = room.getId();
        String msg;
        switch (source) {
        case 139:
            final int x1 = 652, x2 = 650, y = 1337;
            if (super.getIO().getPosition().equals(x1, y)) {
                room.setMainNode(room.getNode(x2, y));
                room.addCommand(FFCommand.WEST);
                room.removeCommand(FFCommand.CLIMB);
                super.getIO().setPosition(new SimpleVector2(x2, y));
                msg = "climb_139_out";
            } else {
                msg = "climb_139_in";
            }
            break;
            default:
                msg = "climb_no_where";
        }
        GameScreen.getInstance().addMessage(
                FFWebServiceClient.getInstance().loadText(msg));
        return ScriptConstants.ACCEPT;
    }
    /**
     * On IO entering room 1.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onEnterRoom1() throws RPGException {
        return ScriptConstants.ACCEPT;
    }
    /**
     * On IO entering room 139.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onEnterRoom139() throws RPGException {
        return ScriptConstants.ACCEPT;
    }
    /**
     * On IO entering room 12.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onEnterRoom12() throws RPGException {
        FFRoomNode room = FFWorldMap.getInstance().getRoom(12);
        if (!room.isVisited()) {
            FFInteractiveObject io =
                    FFWebServiceClient.getInstance().loadNPC("DOOR_12");
            io.setScriptLoaded(true);
        }
        return ScriptConstants.ACCEPT;
    }
    /**
     * On IO entering room 71.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onEnterRoom71() throws RPGException {
        FFRoomNode room = FFWorldMap.getInstance().getRoom(12);
        if (!room.isVisited()) {
            FFInteractiveObject io =
                    FFWebServiceClient.getInstance().loadNPC("ORC_SENTRY");
            io.setScriptLoaded(true);
        }
        return ScriptConstants.ACCEPT;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int onInit() throws RPGException {
        initLocalVars();
        return super.onInit();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int onOuch() throws RPGException {
        ouchStart();
        return ScriptConsts.ACCEPT;
    }
    /**
     * On IO travelling.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int onTravel() throws RPGException {
        setLocalVarBlockedMessage("");
        // get room occupied
        FFRoomNode room = FFWorldMap.getInstance().getPlayerRoom();
        int source = room.getId();
        PhysicalGraphNode destination = null;
        FFCommand direction = FFCommand.valueOf(getLocalVarTravelDirection());
        switch (direction) {
        case EAST:
            destination = getDestinationEast(source);
            break;
        case SOUTH:
            destination = getDestinationSouth(source);
            break;
        case WEST:
            destination = getDestinationWest(source);
            break;
        case NORTH:
            destination = getDestinationNorth(source);
            break;
        default:
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "Cannot run onTravel event for command "
                            + direction.toString());
        }
        if (destination == null) {
            if (getLocalVarBlockedMessage().length() == 0) {
                GameScreen.getInstance().addMessage(
                        FFWebServiceClient.getInstance().loadText(
                                "invalid_exit"));
            } else {
                GameScreen.getInstance().addMessage(
                        getLocalVarBlockedMessage());
            }
        } else {
            // check to see if path is blocked
            if (checkPath(source, destination)) {
                travel(direction, source, destination);
            } else if (getLocalVarBlockedMessage().length() > 0) {
                GameScreen.getInstance()
                        .addMessage(getLocalVarBlockedMessage());
            }
        }
        return ScriptConstants.ACCEPT;
    }
    /**
     * Starts the ouch event.
     * @throws PooledException if an error occurs
     * @throws RPGException if an error occurs
     */
    private void ouchStart() throws RPGException {
        float ouchDmg = getLocalVarSummonedOuch() + getLocalVarOuch();
        // speak combat message first
        if (getLocalVarCombatMessage().length() > 0) {
            Script.getInstance().speak(super.getIO(),
                    new SpeechParameters("",
                            String.format(getLocalVarCombatMessage(),
                                    (int) ouchDmg)));
        }
    }
    /**
     * Sets the local variable "blocked_message".
     * @param val the variable value
     * @throws RPGException if an error occurs
     */
    private void setLocalVarBlockedMessage(final String val)
            throws RPGException {
        super.setLocalVariable("blocked_message", val);
    }
    /**
     * Sets the local variable "combat_message".
     * @param val the variable value
     * @throws RPGException if an error occurs
     */
    public void setLocalVarCombatMessage(final String val)
            throws RPGException {
        super.setLocalVariable("combat_message", val);
    }
    /**
     * Sets the local variable "travel_direction".
     * @param val the variable value
     * @throws RPGException if an error occurs
     */
    private void setLocalVarTravelDirection(final String val)
            throws RPGException {
        super.setLocalVariable("travel_direction", val);
    }
    /**
     * Travels in a specific direction.
     * @param direction the direction
     * @param source the source room id
     * @param destination the destination room id
     * @throws RPGException if an error occurs
     */
    private void travel(final FFCommand direction, final int source,
            final PhysicalGraphNode destination) throws RPGException {
        // are there any IOs in the room besides the PC?
        FFInteractiveObject[] ios =
                FFWorldMap.getInstance().getIosInRoom(
                        FFWorldMap.getInstance().getPlayerRoom());
        FFInteractiveObject[] npcs = null;
        // get all NPCs
        for (int i = ios.length - 1; i >= 0; i--) {
            if (ios[i].hasIOFlag(IoGlobals.IO_03_NPC)
                    && !ios[i].isInGroup("DOORS")) {
                if (npcs == null) {
                    npcs = new FFInteractiveObject[0];
                }
                npcs = ArrayUtilities.getInstance().extendArray(ios[i], npcs);
            }
        }
        if (npcs != null) {
            // alert NPCs of a sound event
            for (int i = npcs.length - 1; i >= 0; i--) {
                Script.getInstance().sendIOScriptEvent(
                        npcs[i], ScriptConstants.SM_46_HEAR, null, null);
                if (Combat.getInstance().isOver()) {
                    // NPC didn't hear or doesn't care PC is moving.
                    // go to destination
                    goToRoom(direction, source, destination);
                }
            }
        } else {
            goToRoom(direction, source, destination);
        }
    }
    private void goToRoom(final FFCommand direction, final int source,
            final PhysicalGraphNode destination) throws RPGException {
        // put hero in destination room
        super.getIO().setPosition(destination.getLocation());
        // add action text
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append(source);
            sb.append("_");
            sb.append(direction.toString());
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        GameScreen.getInstance().addMessage(
                FFWebServiceClient.getInstance().loadText(sb.toString()));
        sb.returnToPool();
        sb = null;
    }
}
