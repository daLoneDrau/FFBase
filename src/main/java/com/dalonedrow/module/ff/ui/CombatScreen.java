package com.dalonedrow.module.ff.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.graph.FFRoomNode;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFCommand;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.systems.Combat;
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
@SuppressWarnings("unchecked")
public final class CombatScreen extends ConsoleView {
    /** the one and only instance of the <code>WelcomeScreen</code> class. */
    private static CombatScreen instance;
    /** the maximum number of lines to show. */
    private static final int MAX_LINES = 13;
    /**
     * Gives access to the singleton instance of {@link CombatScreen}.
     * @return {@link CombatScreen}
     */
    public static CombatScreen getInstance() {
        if (CombatScreen.instance == null) {
            CombatScreen.instance = new CombatScreen();
        }
        return CombatScreen.instance;
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
    /** the message panel. */
    private final Panel pnlMsg;
    /** the panel displaying the stats. */
    private final Panel pnlStatsEnemy;
    /** the panel displaying the stats. */
    private final Panel pnlStatsHero;
    /** the panel displaying "VS.". */
    private final Panel pnlVersus;
    /** the width of the stats table, with borders. */
    private final int widthCommandsTable;
    /** the width of the stats table, with borders. */
    private final int widthStatsTable;
    /** Hidden constructor. */
    private CombatScreen() {
        final int padding = 4;
        final int mapHeight = 11, mapDescHeight = 4;
        final int statsHeight = 5, commandsHeight = 5;
        final int msgHeight = 8;
        final int screenWidth =
                ProjectConstants.getInstance().getConsoleWidth();
        widthStatsTable = "Stamina: 99/99".length() + padding;
        widthCommandsTable = screenWidth;
        pnlMsg = new FFPanel(screenWidth, false, msgHeight);
        pnlAction = new FFPanel(screenWidth, false, 1, "", "");
        pnlStatsHero =
                new FFPanel(widthStatsTable, true, statsHeight, "", "Hero");
        pnlStatsEnemy =
                new FFPanel(widthStatsTable, true, statsHeight, "", "Enemy");
        pnlVersus = new FFPanel(screenWidth - (2 * widthStatsTable),
                false, statsHeight);
        pnlCmds = new FFPanel(widthCommandsTable, true, commandsHeight, "",
                "COMMANDS");
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
        if (!Combat.getInstance().defeated()) {
            FFInteractiveObject player =
                    ((FFController) ProjectConstants.getInstance())
                            .getPlayerIO();
            String[] commands = s.split(" ");
            try {
                FFCommand command =
                        FFCommand.valueOf(commands[0].toUpperCase());
                switch (command) {
                case ATTACK:
                    Combat.getInstance().doRound();
                    break;
                case ESCAPE:
                    if (Combat.getInstance().escapeAllowed()) {
                        Combat.getInstance().clearEnemies();
                        Combat.getInstance().addMessage(1, "ESCAPE!");
                        Script.getInstance().sendIOScriptEvent(
                                player, 0, null, "Escape");
                    } else {
                        Combat.getInstance().addMessage(1,
                                FFWebServiceClient.getInstance().loadText(
                                        "no_escape"));
                    }
                    break;
                default:
                    Combat.getInstance().addMessage(1,
                            FFWebServiceClient.getInstance().loadText(
                                    "invalid_input"));
                }
            } catch (IllegalArgumentException e) {
                Combat.getInstance().addMessage(1,
                        FFWebServiceClient.getInstance().loadText(
                                "invalid_input"));
            }
        } else {
            Combat.getInstance().setLastMessageDisplayed(true);
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
        List<FFCommand> commands =
                new ArrayList<FFCommand>();
        if (!Combat.getInstance().defeated()) {
            commands.add(FFCommand.ATTACK);
            if (Combat.getInstance().escapeAllowed()) {
                commands.add(FFCommand.ESCAPE);
            }
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
        commands = null;
        list = null;
    }
    private void processMessageView() throws RPGException {
        if (Combat.getInstance().getMessages().length > 0) {
            messageText = "";
        }
        for (int i = 0, len = Combat.getInstance().getMessages().length;
                i < len; i++) {
            addMessage(Combat.getInstance().getMessages()[i]);
        }
    }
    /**
     * Processes the content for the status view.
     * @throws RPGException if an error occurs
     */
    private void processStatsView() throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append("                                           ");
            sb.append("           ROUND ");
            sb.append(Script.getInstance().getGlobalIntVariableValue(
                    "COMBATROUND") + 1);
            sb.append("\n\n                              VS.");
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        pnlVersus.setContent(sb.toString());
        sb.returnToPool();
        sb = null;
        String[] stats =
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData().getStatusString();
        String content = TextProcessor.getInstance().processText(null,
                null,
                stats,
                FFWebServiceClient.getInstance().loadText("stats_table"));
        pnlStatsHero.setContent(content);
        stats = Combat.getInstance().getEnemyStats();
        content = TextProcessor.getInstance().processText(null,
                null,
                stats,
                FFWebServiceClient.getInstance().loadText("enemy_stats_table"));
        pnlStatsEnemy.setContent(content);
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
        processMessageView();
        pnlMsg.setContent(messageText);
        if (Combat.getInstance().defeated()) {
            pnlAction.setContent(
                    FFWebServiceClient.getInstance().loadText("continue"));
        } else {
            pnlAction.setContent(
                    FFWebServiceClient.getInstance().loadText("choice"));
        }
        // join commands panel to top of action panel
        pnlCmds.join(pnlAction, Panel.TOP, Panel.CENTER);
        // join message panel to top of commands panel
        pnlMsg.join(pnlCmds, Panel.TOP, Panel.LEFT);
        // join versus panel to left of enemy panel
        pnlVersus.join(pnlStatsEnemy, Panel.LEFT, Panel.CENTER);
        // join hero panel to left of versus panel
        pnlStatsHero.join(pnlVersus, Panel.LEFT, Panel.CENTER);
        OutputEvent.getInstance().print(pnlStatsHero.getDisplayText(), this);
        OutputEvent.getInstance().print(pnlMsg.getDisplayText(), this);
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
