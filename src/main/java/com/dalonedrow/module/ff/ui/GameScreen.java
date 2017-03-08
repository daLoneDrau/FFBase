package com.dalonedrow.module.ff.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.graph.FFRoomNode;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFCommand;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.consoleui.ConsoleView;
import com.dalonedrow.rpg.base.consoleui.InputProcessor;
import com.dalonedrow.rpg.base.consoleui.OutputEvent;
import com.dalonedrow.rpg.base.consoleui.Panel;
import com.dalonedrow.rpg.base.consoleui.TextProcessor;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.ScriptConstants;
import com.dalonedrow.rpg.base.systems.Script;
import com.dalonedrow.utils.ArrayUtilities;

/**
 * @author 588648
 */
@SuppressWarnings("unchecked")
public final class GameScreen extends ConsoleView {
    /** the one and only instance of the <code>WelcomeScreen</code> class. */
    private static GameScreen instance;
    /** the maximum number of lines to show. */
    private static final int MAX_LINES = 13;
    /**
     * Gives access to the singleton instance of {@link GameScreen}.
     * @return {@link GameScreen}
     */
    public static GameScreen getInstance() {
        if (GameScreen.instance == null) {
            GameScreen.instance = new GameScreen();
        }
        return GameScreen.instance;
    }
    /** the current index. */
    private int index;
    private String messageText;
    /** the panel displayed. */
    private Panel panel;
    /** the panel displaying the action prompt. */
    private final Panel pnlAction;
    /** the panel displaying the commands. */
    private final Panel pnlCmds;
    /** the panel displaying the room view. */
    private final Panel pnlMap;
    /** the panel displaying the room description. */
    private final Panel pnlMapDesc;
    /** the message panel. */
    private final Panel pnlMsg;
    /** the panel displaying the stats. */
    private final Panel pnlStats;
    /** the width of the stats table, with borders. */
    private final int widthCommandsTable;
    /** the width of the stats table, with borders. */
    private final int widthStatsTable;
    /** Hidden constructor. */
    private GameScreen() {
        final int padding = 4;
        final int mapHeight = 11, mapDescHeight = 8;
        final int statsHeight = 5, commandsHeight = 5;
        final int msgHeight = 5;
        final int screenWidth =
                ProjectConstants.getInstance().getConsoleWidth();
        widthStatsTable = "Stamina: 99/99".length() + padding;
        widthCommandsTable = screenWidth - widthStatsTable;
        pnlMap = new FFPanel(screenWidth, false, mapHeight, "", "");
        pnlMapDesc = new FFPanel(screenWidth, false, mapDescHeight, "", "");
        pnlMsg = new FFPanel(screenWidth, true, msgHeight, "", "");
        pnlAction = new FFPanel(screenWidth, false, 1, "", "");
        pnlStats =
                new FFPanel(widthStatsTable, true, statsHeight, "", "STATUS");
        pnlCmds = new FFPanel(widthCommandsTable, true, commandsHeight, "",
                "COMMANDS");
        messageText = "";
    }
    /**
     * Processes an "Open" command.
     * @throws RPGException if an error occurs
     */
    private void processOpen() throws RPGException {
        FFInteractiveObject[] ios = FFWorldMap.getInstance().getIosInRoom(
                FFWorldMap.getInstance().getPlayerRoom());
        FFInteractiveObject[] npcs = null;
        if (ios != null) {
            for (int i = ios.length - 1; i >= 0; i--) {
                if (ios[i] != null
                        && ios[i].isInGroup("DOORS")) {
                    if (npcs == null) {
                        npcs = new FFInteractiveObject[0];
                    }
                    npcs = ArrayUtilities.getInstance().extendArray(
                            ios[i], npcs);
                }
            }
        }
        if (npcs != null
                && npcs.length > 0) {
            if (npcs.length == 1) {
                Script.getInstance().sendIOScriptEvent(
                        npcs[0], 0, null, FFCommand.OPEN.getEventName());
            } else {
                addMessage(FFWebServiceClient.getInstance().loadText(
                        "open_which_door"));
            }
        } else {
            addMessage(FFWebServiceClient.getInstance().loadText(
                    "open_no_door"));
        }
        ios = null;
        npcs = null;
    }
    /**
     * Processes a "Smash" command.
     * @throws RPGException if an error occurs
     */
    private void processSmash() throws RPGException {
        FFInteractiveObject[] ios = FFWorldMap.getInstance().getIosInRoom(
                FFWorldMap.getInstance().getPlayerRoom());
        FFInteractiveObject[] npcs = null;
        if (ios != null) {
            for (int i = ios.length - 1; i >= 0; i--) {
                if (ios[i] != null
                        && ios[i].isInGroup("DOORS")) {
                    if (npcs == null) {
                        npcs = new FFInteractiveObject[0];
                    }
                    npcs = ArrayUtilities.getInstance().extendArray(
                            ios[i], npcs);
                }
            }
        }
        if (npcs != null
                && npcs.length > 0) {
            if (npcs.length == 1) {
                Script.getInstance().sendIOScriptEvent(
                        npcs[0], 0, null, FFCommand.SMASH.getEventName());
            } else {
                addMessage(FFWebServiceClient.getInstance().loadText(
                        "smash_which_door"));
            }
        } else {
            addMessage(FFWebServiceClient.getInstance().loadText(
                    "smash_no_door"));
        }
        ios = null;
        npcs = null;
    }
    /**
     * Processes an "Attack" command.
     * @throws RPGException if an error occurs
     */
    private void processAttack() throws RPGException {
        FFInteractiveObject[] ios =
                FFWorldMap.getInstance().getIosInRoom(
                        FFWorldMap.getInstance().getPlayerRoom());
        FFInteractiveObject[] npcs = null;
        if (ios != null) {
            for (int i = ios.length - 1; i >= 0; i--) {
                if (ios[i] != null
                        && ios[i].hasIOFlag(IoGlobals.IO_03_NPC)
                        && !ios[i].getNPCData().IsDeadNPC()
                        && !ios[i].isInGroup("DOORS")) {
                    if (npcs == null) {
                        npcs = new FFInteractiveObject[0];
                    }
                    npcs = ArrayUtilities.getInstance().extendArray(
                            ios[i], npcs);
                }
            }
        }
        if (npcs != null
                && npcs.length > 0) {
            if (npcs.length == 1) {
                Script.getInstance().sendIOScriptEvent(
                        npcs[0], ScriptConstants.SM_057_AGGRESSION,
                        null, null);
            } else {
                addMessage(FFWebServiceClient.getInstance().loadText(
                        "attack_which_npc"));
            }
        } else {
            addMessage(FFWebServiceClient.getInstance().loadText(
                    "attack_no_one"));
        }
        ios = null;
        npcs = null;
    }
    /**
     * Processes user input to go to the next screen.
     * @param s user input
     * @throws RPGException if an error occurs
     */
    public void actionProcessInput(final String s) throws RPGException {
        // clear old messages
        messageText = "";
        FFInteractiveObject player =
                ((FFController) ProjectConstants.getInstance()).getPlayerIO();
        String[] commands = s.split(" ");
        try {
            FFCommand command = FFCommand.valueOf(commands[0].toUpperCase());
            switch (command) {
            case EAST:
            case SOUTH:
            case NORTH:
            case WEST:
                Script.getInstance().sendIOScriptEvent(player, 0, new Object[] {
                        "travel_direction", command.toString()
                }, "Travel");
                break;
            case ATTACK:
                processAttack();
                break;
            case CLIMB:
                Script.getInstance().sendIOScriptEvent(
                        player, 0, null, command.getEventName());
                break;
            case SMASH:
                this.processSmash();
                break;
            case OPEN:
                this.processOpen();
                break;
            default:
            }
        } catch (IllegalArgumentException e) {
            addMessage(FFWebServiceClient.getInstance().loadText(
                    "invalid_input"));
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void addErrorMessage(final String msg) {
        // TODO Auto-generated method stub

    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void addMessage(final String text) throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append(messageText);
            if (messageText.length() > 0) {
                sb.append("\n");
            }
            sb.append(text);
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        messageText = sb.toString();
        sb.returnToPool();
        sb = null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * Processes the content for the status view.
     * @throws RPGException if an error occurs
     */
    private void processCommandsView() throws RPGException {
        SimpleVector2 pos = ((FFController) ProjectConstants.getInstance())
                .getPlayerIO().getPosition();
        FFRoomNode room =
                FFWorldMap.getInstance().getRoomByCellCoordinates(pos);
        List<FFCommand> commands =
                new ArrayList<FFCommand>(Arrays.asList(room.getCommands()));
        commands.add(FFCommand.SEARCH);
        commands.add(FFCommand.USE);
        commands.add(FFCommand.INVENTORY);
        boolean hasDoor = false;
        FFInteractiveObject[] ios = FFWorldMap.getInstance().getIosInRoom(
                FFWorldMap.getInstance().getPlayerRoom());
        if (ios != null) {
            for (int i = ios.length - 1; i >= 0; i--) {
                if (ios[i] != null
                        && ios[i].isInGroup("DOORS")) {
                    hasDoor = true;
                    break;
                }
            }
        }
        if (hasDoor) {
            commands.add(FFCommand.OPEN);
            commands.add(FFCommand.SMASH);
        }
        boolean hasNPC = false;
        if (ios != null) {
            for (int i = ios.length - 1; i >= 0; i--) {
                if (ios[i] != null
                        && ios[i].hasIOFlag(IoGlobals.IO_03_NPC)
                        && !ios[i].getNPCData().IsDeadNPC()
                        && !ios[i].isInGroup("DOORS")) {
                    hasNPC = true;
                    break;
                }
            }
        }
        if (hasNPC) {
            commands.add(FFCommand.ATTACK);
        }
        Collections.sort(commands, FFCommandComparator.getInstance());
        List<String> list = new ArrayList<String>();
        int lenw = 0;
        for (int i = 0, len = commands.size(); i < len; i++) {
            list.add(commands.get(i).name());
            lenw += commands.get(i).name().length();
        }
        lenw += commands.size() * 3;
        if (lenw < widthCommandsTable - 4) {
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            for (int i = widthCommandsTable - 4 - lenw; i > 0; i--) {
                try {
                    sb.append(' ');
                } catch (PooledException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
                }
            }
            list.add(sb.toString());
            sb.returnToPool();
            sb = null;
        }
        pnlCmds.setContent(
                TextProcessor.getInstance().getSelectionsAsColumns(9,
                        list.toArray(new String[list.size()]), "   "));
        pos = null;
        room = null;
        commands = null;
        list = null;
    }
    /**
     * Processes the content for the status view.
     * @throws RPGException if an error occurs
     */
    private void processMapView() throws RPGException {
        pnlMap.setContent(FFWorldMap.getInstance().renderViewport());
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        FFRoomNode room = FFWorldMap.getInstance().getPlayerRoom();
        try {
            sb.append(room.getDisplayText());
            FFInteractiveObject[] ios =
                    FFWorldMap.getInstance().getIosInRoom(
                            FFWorldMap.getInstance().getPlayerRoom());
            for (int i = 0, len = ios.length; i < len; i++) {
                if (ios[i].hasIOFlag(IoGlobals.IO_01_PC)) {
                    ios = ArrayUtilities.getInstance().removeIndex(i, ios);
                    i--;
                    len = ios.length;
                }
            }
            if (ios.length > 0) {
                sb.append("\n");
                sb.append("You see:\t");
                for (int i = ios.length - 1; i >= 0; i--) {
                    if (ios.length > 1 && i < ios.length - 1) {
                        sb.append("\t\t");
                    }
                    if (ios[i].hasIOFlag(IoGlobals.IO_03_NPC)) {
                        sb.append(new String(ios[i].getNPCData().getTitle()));
                    } else if (ios[i].hasIOFlag(IoGlobals.IO_02_ITEM)) {
                        sb.append(new String(ios[i].getItemData().getTitle()));
                    }
                    sb.append("\n");
                }
            }
            ios = null;
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        pnlMapDesc.setContent(sb.toString());
        if (!room.wasInitialTextDisplayed()) {
            room.setInitialTextDisplayed(true);
        }
        sb.returnToPool();
        sb = null;
    }
    /**
     * Processes the content for the status view.
     * @throws RPGException if an error occurs
     */
    private void processStatsView() throws RPGException {
        String[] stats =
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData().getStatusString();
        String content = TextProcessor.getInstance().processText(null,
                null,
                stats,
                FFWebServiceClient.getInstance().loadText("stats_table"));
        pnlStats.setContent(content);
        stats = null;
        content = null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void render() throws RPGException {
        processStatsView();
        processCommandsView();
        processMapView();
        pnlMsg.setContent(messageText);
        pnlAction.setContent(
                FFWebServiceClient.getInstance().loadText("choice"));
        // join command panel to left of stats panel
        pnlCmds.join(pnlStats, Panel.LEFT, Panel.TOP);
        // join commands panel to top of action panel
        pnlCmds.join(pnlAction, Panel.TOP, Panel.CENTER);
        // join room description panel to top of commands panel
        pnlMapDesc.join(pnlCmds, Panel.TOP, Panel.CENTER);
        // join message panel to top of room description panel
        pnlMsg.join(pnlMapDesc, Panel.TOP, Panel.CENTER);
        // join map panel to top of map description panel
        pnlMap.join(pnlMsg, Panel.TOP, Panel.CENTER);
        OutputEvent.getInstance().print(pnlMap.getDisplayText(), this);
        try {
            InputProcessor.getInstance().setInputAction(
                    this, // object
                    getClass().getMethod("actionProcessInput",
                            new Class[] { String.class }), // method
                    null); // arguments to be read from system.in
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
    }
}
