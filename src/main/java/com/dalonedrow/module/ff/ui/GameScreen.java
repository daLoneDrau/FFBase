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
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.Script;

/**
 * @author drau
 */
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
    /** the panel displaying the action prompt. */
    private final Panel actionPanel;
    /** the panel displaying the commands. */
    private final Panel cmdsPanel;
    /** the width of the stats table, with borders. */
    private final int COMMANDS_TABLE_WIDTH;
    /** the current index. */
    private int index;
    /** the panel displaying the room description. */
    private final Panel mapDescPanel;
    /** the panel displaying the room view. */
    private final Panel mapPanel;
    /** the panel displayed. */
    private Panel panel;
    /** the width of the stats table, with borders. */
    private final int STATS_TABLE_WIDTH;
    /** the panel displaying the stats. */
    private final Panel statsPanel;
    /** the intro text. */
    private String[] text;
    /** Hidden constructor. */
    private GameScreen() {
        final int padding = 4;
        final int screenWidth =
                ProjectConstants.getInstance().getConsoleWidth();
        STATS_TABLE_WIDTH = "Stamina: 99/99".length() + padding;
        COMMANDS_TABLE_WIDTH = screenWidth - STATS_TABLE_WIDTH;
        mapPanel = new FFPanel(screenWidth, false, 11, "", "");
        mapDescPanel = new FFPanel(screenWidth, false, 4, "", "");
        actionPanel = new FFPanel(screenWidth, false, 1, "", "");
        statsPanel = new FFPanel(STATS_TABLE_WIDTH, true, 5, "", "STATUS");
        cmdsPanel = new FFPanel(COMMANDS_TABLE_WIDTH, true, 5, "", "COMMANDS");
    }
    /**
     * Processes user input to go to the next screen.
     * @param s not used
     * @throws RPGException 
     * @throws Exception if an error occurs
     */
    public void actionProcessInput(final String s) throws RPGException {
        FFInteractiveObject player =
                ((FFController) FFController.getInstance()).getPlayerIO();
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
    /**
     * {@inheritDoc}
     */
    @Override
    public void addMessage(String text) {
        // TODO Auto-generated method stub

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
        Collections.sort(commands);
        List<String> list = new ArrayList<String>();
        int lenw = 0;
        for (int i = 0, len = commands.size(); i < len; i++) {
            list.add(commands.get(i).name());
            lenw += commands.get(i).name().length();
        }
        lenw += commands.size() * 3;
        if (lenw < COMMANDS_TABLE_WIDTH - 4) {
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            for (int i = COMMANDS_TABLE_WIDTH - 4 - lenw; i > 0; i--) {
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
        cmdsPanel.setContent(
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
        mapPanel.setContent(FFWorldMap.getInstance().renderViewport());
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        FFRoomNode room = FFWorldMap.getInstance().getPlayerRoom();
        mapDescPanel.setContent(room.getDisplayText());
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
        statsPanel.setContent(content);
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
        actionPanel.setContent(
                FFWebServiceClient.getInstance().loadText("choice"));
        cmdsPanel.join(statsPanel, Panel.LEFT, Panel.TOP);
        cmdsPanel.join(actionPanel, Panel.TOP, Panel.CENTER);
        mapPanel.join(cmdsPanel, Panel.TOP, Panel.CENTER);
        OutputEvent.getInstance().print(mapPanel.getDisplayText(), this);
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
