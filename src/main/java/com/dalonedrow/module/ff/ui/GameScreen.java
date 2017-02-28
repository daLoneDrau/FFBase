package com.dalonedrow.module.ff.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.Script;

/**
 * @author 588648
 */
final class FFCommandComparator implements Comparator<FFCommand> {
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FFCommand o1, final FFCommand o2) {
        int compares = 0;
        if (o1.getSortOrder() < o2.getSortOrder()) {
            compares = -1;
        } else if (o1.getSortOrder() < o2.getSortOrder()) {
            compares = 1;
        }
        return compares;
    }
}

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
    /** command sorter. */
    private FFCommandComparator commandSorter;
    /** the current index. */
    private int index;
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
        final int mapHeight = 11, mapDescHeight = 4;
        final int statsHeight = 5, commandsHeight = 5;
        final int screenWidth =
                ProjectConstants.getInstance().getConsoleWidth();
        widthStatsTable = "Stamina: 99/99".length() + padding;
        widthCommandsTable = screenWidth - widthStatsTable;
        pnlMap = new FFPanel(screenWidth, false, mapHeight, "", "");
        pnlMapDesc = new FFPanel(screenWidth, false, mapDescHeight, "", "");
        pnlMsg = new FFPanel(screenWidth, true, 3, "", "");
        pnlAction = new FFPanel(screenWidth, false, 1, "", "");
        pnlStats =
                new FFPanel(widthStatsTable, true, statsHeight, "", "STATUS");
        pnlCmds = new FFPanel(widthCommandsTable, true, commandsHeight, "",
                "COMMANDS");
        commandSorter = new FFCommandComparator();
        messageText = "";
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
        if (FFCommand.EAST.name().equalsIgnoreCase(s)) {
            Script.getInstance().sendIOScriptEvent(player, 0, null, "East");
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void addErrorMessage(final String msg) {
        // TODO Auto-generated method stub

    }
    private String messageText;
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
        Collections.sort(commands, commandSorter);
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
        pnlMapDesc.setContent(room.getDisplayText());
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
