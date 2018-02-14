package com.dalonedrow.module.ff.systems;

import com.dalonedrow.engine.systems.base.Diceroller;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.rpg.FFCharacter;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFNpc;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.constants.EquipmentGlobals;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.SpeechParameters;
import com.dalonedrow.rpg.base.systems.CombatUtility;
import com.dalonedrow.rpg.base.systems.Script;
import com.dalonedrow.utils.ArrayUtilities;

/**
 * @author 588648
 */
@SuppressWarnings("unchecked")
public final class Combat extends CombatUtility<FFInteractiveObject> {
    private static final int HIT_LUK = 1;
    private static final int HIT_STD = 0;
    private static final int HIT_UNLUK = 2;
    private static final int HURT_LUK = 4;
    private static final int HURT_STD = 3;
    private static final int HURT_UNLUK = 5;
    private static Combat instance;
    /** the maximum number of messages displayed. */
    private static final int MAX_MESSAGES = 8;
    /** the code for error messages. */
    public static final int MESSAGE_ERROR = 0;
    /** the code for info messages. */
    public static final int MESSAGE_INFO = 4;
    /** the code for positive messages. */
    public static final int MESSAGE_POSITIVE = 3;
    /** the code for standard messages. */
    public static final int MESSAGE_STANDARD = 1;
    /** the code for warning messages. */
    public static final int MESSAGE_WARNING = 2;
    /** testing mode - both combatants miss. */
    protected static final int TEST_MODE_BOTH_MISS = 7;
    /** testing mode - creature hits. */
    protected static final int TEST_MODE_CREATURE_HITS = 4;
    /** testing mode - creature hits but player lucky. */
    protected static final int TEST_MODE_CREATURE_HITS_LUCKY = 5;
    /** testing mode - creature hits and player unlucky. */
    protected static final int TEST_MODE_CREATURE_HITS_UNLUCKY = 6;
    /** testing off. */
    protected static final int TEST_MODE_OFF = 0;
    /** testing mode - player hits. */
    protected static final int TEST_MODE_PLAYER_HITS = 1;
    /** testing mode - player hits and player lucky. */
    protected static final int TEST_MODE_PLAYER_HITS_LUCKY = 2;
    /** testing mode - player hits but player unlucky. */
    protected static final int TEST_MODE_PLAYER_HITS_UNLUCKY = 3;
    /**
     * Gives access to the singleton instance of {@link Script}.
     * @return {@link Script}
     */
    public static Combat getInstance() {
        return Combat.instance;
    }
    /** the list of enemies. */
    private FFInteractiveObject[] enemies = new FFInteractiveObject[0];
    /** the list of error messages. */
    private String[] errorMessages;
    private boolean lastMessageDisplayed;
    /** the flag indicating luck was applied. */
    private boolean luckApplied;
    /** the list of messages. */
    private String[] messages;
    /** the list of message types. */
    private int[] messageTypes;
    /** flag for testing mode. */
    private int testingMode;
    /** Creates a new instance of {@link Combat}. */
    protected Combat() {
        instance = this;
        messages = new String[0];
        errorMessages = new String[0];
        messageTypes = new int[0];
        lastMessageDisplayed = true;
    }
    /**
     * Adds an enemy to battle.
     * @param io the enemy {@link FFInteractiveObject}
     * @throws RPGException if an error occurs
     */
    public void addEnemy(final FFInteractiveObject io) throws RPGException {
        if (io == null) {
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "Cannot add null enemy");
        }
        if (!io.hasIOFlag(IoGlobals.IO_03_NPC)) {
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "Enemy must be NPC");
        }
        boolean found = false;
        for (int i = enemies.length - 1; i >= 0; i--) {
            if (io.getRefId() == enemies[i].getRefId()) {
                found = true;
                break;
            }
        }
        if (!found) {
            enemies = ArrayUtilities.getInstance().extendArray(io, enemies);
        }
        lastMessageDisplayed = false;
    }
    /**
     * Adds a message to be displayed in the GUI.
     * @param type the type of message
     * @param message the message text
     */
    public void addMessage(final int type, final String message) {
        switch (type) {
        case MESSAGE_ERROR:
            errorMessages = ArrayUtilities.getInstance().extendArray(
                    message, errorMessages);
            break;
        default:
            messageTypes = ArrayUtilities.getInstance().extendArray(
                    type, messageTypes);
            messages = ArrayUtilities.getInstance().extendArray(
                    message, messages);
        }
    }
    private void checkDead() throws RPGException {
        for (int i = enemies.length - 1; i >= 0; i--) {
            FFNpc enemy = enemies[i].getNPCData();
            enemy.computeFullStats();
            if (enemy.IsDeadNPC()) {
                // Script.getInstance().sendIOScriptEvent(target, msg, params,
                // eventname)
            }
        }
    }
    /** Clears all messages beyond max. */
    public void clearExcessMessages() {
        while (getMessageLength() > MAX_MESSAGES) {
            messages = ArrayUtilities.getInstance().removeIndex(0, messages);
            messageTypes = ArrayUtilities.getInstance().removeIndex(
                    0, messageTypes);
        }
    }
    /** Clears all messages. */
    public void clearMessages() {
        messages = new String[0];
        messageTypes = new int[0];
    }
    /**
     * Creates the message displayed following a LUCKY hit.
     * @param source the player's weapon
     * @param target the target being struck
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
    private String createHitLuckyMessage(final String source,
            final String target) throws RPGException {
        String s;
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("CRITICAL STRIKE!\n");
                sb.append("Your %s goes snicker-snack! causing %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), source, "%d");
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("CRITICAL STRIKE!\n");
                sb.append("Burbling as you come you strike swiftly; ");
                sb.append("%s takes %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), target, "%d");
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("CRITICAL STRIKE!\n");
                sb.append("Like a pitiless croupier you deal %s a losing ");
                sb.append("hand: %s hearts of DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), target, "%d");
            sb.returnToPool();
            sb = null;
            break;
        }
        return s;
    }
    /**
     * Creates the message displayed following a STANDARD hit.
     * @param target the target being struck
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
    private String createHitMessage(final String target)
            throws RPGException {
        String s;
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("HIT!\n");
                sb.append("%s takes %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), target, "%d");
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("HIT!\n");
                sb.append("%s is becoming worn out by your relentless ");
                sb.append("attacks and takes %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), target, "%d");
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("HIT!\n");
                sb.append("You bypass the enemy's guard and strike %s for ");
                sb.append("%s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), target, "%d");
            sb.returnToPool();
            sb = null;
            break;
        }
        return s;
    }
    /**
     * Creates the message displayed following an UNLUCKY hit.
     * @param source the player's weapon
     * @param target the target being struck
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
    private String createHitUnluckyMessage(final String source,
            final String target) throws RPGException {
        String s;
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("GLANCING BLOW!\n");
                sb.append("Your attack is off-balance, striking %s ");
                sb.append("without much force; %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), target, "%d");
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("GLANCING BLOW!\n");
                sb.append("%s rolls with your blow, ");
                sb.append("lessening the DAMAGE to %s point.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), target, "%d");
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("GLANCING BLOW!\n");
                sb.append("You swing your %s wildly, landing at an awkward ");
                sb.append("angle; %s takes %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), source, target, "%d");
            sb.returnToPool();
            sb = null;
            break;
        }
        return s;
    }
    /**
     * Creates the message displayed following a LUCKY hit from the ENEMY.
     * @param enemy the enemy
     * @param weapon the enemy's weapon
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
    private String createHurtLuckyMessage(final String enemy,
            final String weapon) throws RPGException {
        String s;
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s GLANCING BLOW!\n");
                sb.append("The %s strikes you a glancing blow for %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, enemy, "%d");
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s GLANCING BLOW!\n");
                sb.append("You dodge most of the %s, but the edge of it ");
                sb.append("catches your ribs for %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, weapon, "%d");
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s GLANCING BLOW!\n");
                sb.append("%s catches you off-guard, but without much force ");
                sb.append("behind the strike you only take %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, enemy, "%d");
            sb.returnToPool();
            sb = null;
            break;
        }
        return s;
    }
    /**
     * Creates the message displayed following a STANDARD hit from the ENEMY.
     * @param enemy the enemy
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
    private String createHurtMessage(final String enemy)
            throws RPGException {
        String s;
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s HITS!\n");
                sb.append("You take %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, "%d");
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s HITS!\n");
                sb.append("%s shifts their weight and then suddenly ");
                sb.append("launches an attack; you take %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, enemy, "%d");
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s HITS!\n");
                sb.append("You retreat slightly after that last blow gave ");
                sb.append("you %s points of DAMAGE; %s looks confident.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, "%d", enemy);
            sb.returnToPool();
            sb = null;
            break;
        }
        return s;
    }
    /**
     * Creates the message displayed following an UNLUCKY hit from the ENEMY.
     * @param enemy the enemy
     * @param weapon the enemy's weapon
     * @return {@link String}
     * @throws RPGException if an error occurs
     */
    private String createHurtUnluckyMessage(final String enemy,
            final String weapon) throws RPGException {
        String s;
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s CRITICAL STRIKE!\n");
                sb.append("The manxome foe's %s goes through and through; ");
                sb.append("%s DAMAGE taken.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, weapon, "%d");
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s CRITICAL STRIKE!\n");
                sb.append("Moving swifter than you would have thought ");
                sb.append("possible, %s deals you a painful blow. ");
                sb.append("You take %s DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, enemy, "%d");
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s CRITICAL STRIKE!\n");
                sb.append("You rush at the %s but a flurry of blows forces ");
                sb.append("you back; you take %s points of DAMAGE.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            s = String.format(sb.toString(), enemy, enemy, "%d");
            sb.returnToPool();
            sb = null;
            break;
        }
        return s;
    }
    public boolean defeated() throws RPGException {
        boolean over = false;
        if (((FFController) ProjectConstants.getInstance()).getPlayerIO()
                .getPCData().getFullAttributeScore("ST") <= 0f) {
            over = true;
        }
        if (!over) {
            over = enemies.length == 0;
        }
        return over;
    }
    @Override
    public void doRound() throws RPGException {
        if (enemies.length > 0) {
            // declare variables
            String source, target;
            int msgMode;
            FFInteractiveObject enemyIO = enemies[0].getNPCData().getIo();
            FFNpc enemy = enemies[0].getNPCData();
            FFInteractiveObject playerIO =
                    (FFInteractiveObject) Interactive.getInstance().getIO(
                            ProjectConstants.getInstance().getPlayer());
            FFCharacter pc = (FFCharacter) Interactive.getInstance().getIO(
                    ProjectConstants.getInstance().getPlayer()).getPCData();
            // 1. get creature's Attack Strength.
            float enemyAttackStrength = enemy.getFullAttributeScore("SK")
                    + Diceroller.getInstance().rollXdY(2, 6);
            // 2. get player's Attack Strength.
            float playerAttackStrength = pc.getFullAttributeScore("SK")
                    + Diceroller.getInstance().rollXdY(2, 6);
            // 3. compare attack strengths
            if ((testingMode == TEST_MODE_PLAYER_HITS
                    || testingMode == TEST_MODE_PLAYER_HITS_LUCKY
                    || testingMode == TEST_MODE_PLAYER_HITS_UNLUCKY
                    || testingMode == TEST_MODE_CREATURE_HITS
                    || testingMode == TEST_MODE_CREATURE_HITS_LUCKY
                    || testingMode == TEST_MODE_CREATURE_HITS_UNLUCKY
                    || playerAttackStrength != enemyAttackStrength)
                    && testingMode != TEST_MODE_BOTH_MISS) {
                msgMode = HIT_STD;
                int wpnId;
                float luckMod = 1;
                FFInteractiveObject srcIO, trgtIO, wpnIO;
                // 4. pc wounds creature
                if ((playerAttackStrength > enemyAttackStrength
                        || testingMode == TEST_MODE_PLAYER_HITS
                        || testingMode == TEST_MODE_PLAYER_HITS_LUCKY
                        || testingMode == TEST_MODE_PLAYER_HITS_UNLUCKY)
                        && testingMode != TEST_MODE_CREATURE_HITS
                        && testingMode != TEST_MODE_CREATURE_HITS_LUCKY
                        && testingMode != TEST_MODE_CREATURE_HITS_UNLUCKY) {
                    target = new String(enemyIO.getNPCData().getTitle());
                    wpnId = pc.getEquippedItem(
                            EquipmentGlobals.EQUIP_SLOT_WEAPON);
                    source = new String(Interactive.getInstance().getIO(
                            wpnId).getItemData().getItemName());
                    srcIO = playerIO;
                    trgtIO = enemyIO;
                    if (luckApplied
                            || testingMode == TEST_MODE_PLAYER_HITS_LUCKY
                            || testingMode == TEST_MODE_PLAYER_HITS_UNLUCKY) {
                        if (pc.testYourLuck(true)
                                || testingMode == TEST_MODE_PLAYER_HITS_LUCKY) {
                            // if lucky, damage is doubled
                            msgMode = HIT_LUK;
                            luckMod = 2;
                        } else {
                            // if unlucky, damage is halved
                            msgMode = HIT_UNLUK;
                            luckMod = 0.5f;
                        }
                    }
                    if (((FFController) ProjectConstants.getInstance())
                            .godMode()) {
                        luckMod = 50f;
                    }
                } else {
                    // 5. creature wounds pc
                    msgMode = HURT_STD;
                    wpnId = enemy.getEquippedItem(
                            EquipmentGlobals.EQUIP_SLOT_WEAPON);
                    srcIO = enemyIO;
                    trgtIO = playerIO;
                    target = new String(Interactive.getInstance().getIO(
                            wpnId).getItemData().getItemName());
                    source = new String(enemyIO.getNPCData().getTitle());
                    if (luckApplied
                            || testingMode == TEST_MODE_CREATURE_HITS_LUCKY
                            || testingMode == TEST_MODE_CREATURE_HITS_UNLUCKY) {
                        if (pc.testYourLuck(true)
                                || testingMode == TEST_MODE_CREATURE_HITS_LUCKY) {
                            // if lucky, damage is halved
                            msgMode = HURT_LUK;
                            luckMod = 0.5f;
                        } else {
                            // if unlucky, damage is increased 150%
                            msgMode = HURT_UNLUK;
                            luckMod = 1.5f;
                        }
                    }
                }
                if (Interactive.getInstance().hasIO(wpnId)) {
                    wpnIO = (FFInteractiveObject) Interactive.getInstance()
                            .getIO(wpnId);
                } else {
                    throw new RPGException(ErrorMessage.INTERNAL_ERROR,
                            "Attacker has no weapon!");
                }
                String s;
                switch (msgMode) {
                case HIT_STD:
                    s = createHitMessage(target);
                    break;
                case HIT_LUK:
                    s = createHitLuckyMessage(source, target);
                    break;
                case HIT_UNLUK:
                    s = createHitUnluckyMessage(source, target);
                    break;
                case HURT_STD:
                    s = createHurtMessage(source);
                    break;
                case HURT_LUK:
                    s = createHurtLuckyMessage(source, target);
                    break;
                default:
                    s = createHurtUnluckyMessage(source, target);
                    break;
                }
                enemyIO.getScript().setLocalVariable("combat_message", s);
                playerIO.getScript().setLocalVariable("combat_message", s);
                wpnIO.getItemData().ARX_EQUIPMENT_ComputeDamages(
                        srcIO, trgtIO, luckMod);
                enemyIO.getScript().setLocalVariable("combat_message", "");
                playerIO.getScript().setLocalVariable("combat_message", "");
            } else {
                Script.getInstance().speak(
                        playerIO, new SpeechParameters("", "MISS!"));
            }
        }
        endRound();
    }
    /** Clears all enemies. */
    public void clearEnemies() {
        int i = enemies.length - 1;
        for (; i >= 0; i--) {
            enemies = ArrayUtilities.getInstance().removeIndex(i, enemies);
        }
    }
    /**
     * Ends the combat round, removing dead enemies and incrementing the combat
     * round.
     * @throws RPGException if an error occurs
     */
    private void endRound() throws RPGException {
        clearExcessMessages();
        // check to see if combat is over.
        int i = enemies.length - 1;
        for (; i >= 0; i--) {
            System.out.println("check if dead enemy " + enemies[i].getRefId());
            if (enemies[i].getNPCData().IsDeadNPC()) {
                System.out.println("enemy dead! removing");
                enemies = ArrayUtilities.getInstance().removeIndex(i, enemies);
            }
        }
        if (!defeated()) { // increment round number
            Script.getInstance().setGlobalVariable("COMBATROUND",
                    Script.getInstance().getGlobalIntVariableValue(
                            "COMBATROUND") + 1);
        }
    }
    /**
     * Determines if an escape attempt is allowed.
     * @return <tt>true</tt> if an escape attempt is allowed; <tt>false</tt>
     *         otherwise
     * @throws RPGException if an error occurs
     */
    public boolean escapeAllowed() throws RPGException {
        boolean allowed = false;
        if (enemies.length > 0) {
            System.out.println("have enemies");
            if (enemies[0].getScript().hasLocalVariable("escape_allowed")
                    && enemies[0].getScript().getLocalIntVariableValue(
                            "escape_allowed") == 1) {
                allowed = true;
            }
            if (!allowed) {
                int round = Script.getInstance().getGlobalIntVariableValue(
                        "COMBATROUND");
                System.out.println("round "+round);
                // check to see if escape during a specific round is okay
                if (round == 0
                        && enemies[0].getScript().hasLocalVariable(
                                "escape_first_round")
                        && enemies[0].getScript().getLocalIntVariableValue(
                                "escape_first_round") == 1) {
                    allowed = true;
                }
                if (!allowed
                        && round > 0
                        && enemies[0].getScript().hasLocalVariable(
                                "escape_after_first_round")
                        && enemies[0].getScript().getLocalIntVariableValue(
                                "escape_after_first_round") == 1) {
                    allowed = true;
                }
            }
        }
        return allowed;
    }
    /**
     * Gets the current enemy's status strings.
     * @return {@link String}[]
     * @throws RPGException if an error occurs
     */
    public String[] getEnemyStats() throws RPGException {
        String[] s = { "", "", "" };
        if (enemies.length > 0) {
            s = enemies[0].getNPCData().getStatusString();
        }
        return s;
    }
    private int getMessageLength() {
        int len = 0;
        for (int i = messages.length - 1; i >= 0; i--) {
            String[] split = messages[i].split("\n");
            len += split.length;
            split = null;
        }
        return len;
    }
    /**
     * Gets the value for the messages.
     * @return {@link String[]}
     */
    public String[] getMessages() {
        return messages;
    }
    /**
     * @return the lastMessageDisplayed
     */
    public boolean isLastMessageDisplayed() {
        return lastMessageDisplayed;
    }
    public boolean isOver() throws RPGException {
        return defeated() && lastMessageDisplayed;
    }
    private void removeEnemy(final FFInteractiveObject io) throws RPGException {
        int index = -1;
        for (int i = enemies.length - 1; i >= 0; i--) {
            if (enemies[i].equals(io)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "Cannot remove enemy " + io.getRefId());
        }
        enemies = ArrayUtilities.getInstance().removeIndex(index, enemies);
    }
    private void reset() {
        clearMessages();
    }
    /**
     * @param lastMessageDisplayed the lastMessageDisplayed to set
     */
    public void setLastMessageDisplayed(boolean flag) {
        lastMessageDisplayed = flag;
        if (flag) {
            reset();
        }
    }
    /**
     * @param testingMode the testingMode to set
     */
    protected void setTestingMode(int testingMode) {
        this.testingMode = testingMode;
    }
    @Override
    protected void tryToHit() {
        // TODO Auto-generated method stub

    }
}
