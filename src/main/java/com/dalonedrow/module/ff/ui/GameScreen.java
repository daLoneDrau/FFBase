package com.dalonedrow.module.ff.ui;

import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.rpg.base.consoleui.ConsoleView;
import com.dalonedrow.rpg.base.consoleui.InputProcessor;
import com.dalonedrow.rpg.base.consoleui.OutputEvent;
import com.dalonedrow.rpg.base.consoleui.Panel;
import com.dalonedrow.rpg.base.consoleui.TextProcessor;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;

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
    private String[] commandList = new String[] {
            "NORTH", "SOUTH", "EAST", "WEST", "OPEN", "SMASH", "ATTACK",
            "BRIBE", "INTIMIDATE", "SEARCH", "USE", "GIVE", "TAKE", "TALK",
            "THROW", "EAT", "STEP", "SIT"
    };
    /** the current index. */
    private int index;
    /** the panel displayed. */
    private Panel panel;
    /** the width of the stats table, with borders. */
    private final int STATS_TABLE_WIDTH;
    /** the width of the stats table, with borders. */
    private final int COMMANDS_TABLE_WIDTH;
    /** the panel displaying the commands. */
    private final Panel cmdsPanel;
    /** the panel displaying the stats. */
    private final Panel statsPanel;
    /** the intro text. */
    private String[] text;
    /** the panel displaying the room view. */
    private final Panel mapPanel;
    /** Hidden constructor. */
    private GameScreen() {
        STATS_TABLE_WIDTH = "Stamina: 99/99".length() + 4;
        COMMANDS_TABLE_WIDTH = 100 - STATS_TABLE_WIDTH;
        mapPanel = new FFPanel(100, false, 11, "", "");
        statsPanel = new FFPanel(STATS_TABLE_WIDTH, true, 5, "", "STATUS");
        cmdsPanel = new FFPanel(COMMANDS_TABLE_WIDTH, true, 5, "", "COMMANDS");
    }
    /**
     * Processes user input to go to the next screen.
     * @param s not used
     * @throws Exception if an error occurs
     */
    public void actionProcessInput(final String s) {}
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
    private void processMapView() throws RPGException {
        mapPanel.setContent(FFWorldMap.getInstance().renderViewport());
    }
    /**
     * Processes the content for the status view.
     * @throws RPGException if an error occurs
     */
    private void processCommandsView() throws RPGException {
        cmdsPanel.setContent(
                TextProcessor.getInstance().getSelectionsAsColumns(9,
                        commandList, "   "));
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
        cmdsPanel.join(statsPanel, Panel.LEFT, Panel.TOP);
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
