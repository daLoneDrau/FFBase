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
import com.dalonedrow.rpg.base.systems.CombatUtility;
import com.dalonedrow.rpg.base.systems.Script;
import com.dalonedrow.utils.ArrayUtilities;

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
        enemies = ArrayUtilities.getInstance().extendArray(io, enemies);
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
        while (messages.length > MAX_MESSAGES) {
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
    private void createHitLuckMessage(final String source,
            final String target, final int damage) throws RPGException {
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            addMessage(MESSAGE_STANDARD,
                    String.format(
                            "Your %s goes snicker-snack! causing %d damage.",
                            source, damage));
            break;
        case 2:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("You swing your %s in a savage fury; ");
                sb.append("%s takes %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), source, target, damage));
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("Like a pitiless croupier you deal your ");
                sb.append("manxome foe %d points of damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD, String.format(sb.toString(), damage));
            sb.returnToPool();
            sb = null;
            break;
        }
    }
    private void createHitMessage(final String target, final int damage)
            throws RPGException {
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            addMessage(MESSAGE_STANDARD,
                    String.format("You hit %s for %d damage.", target, damage));
            break;
        case 2:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s is becoming worn out by your relentless ");
                sb.append("attacks and takes %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), target, damage));
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("You bypass the enemy's guard and strike %s for ");
                sb.append("%d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), target, damage));
            sb.returnToPool();
            sb = null;
            break;
        }
    }
    private void createHurtMessage(final String source, final int damage)
            throws RPGException {
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            addMessage(MESSAGE_STANDARD,
                    String.format("%s hits you for %d damage.", source, damage));
            break;
        case 2:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s shifts their weight and then suddenly ");
                sb.append("launches an attack; you take %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), source, damage));
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("You retreat slightly after that last blow gave ");
                sb.append("you %d points of damage; %s looks confident.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), damage, source));
            sb.returnToPool();
            sb = null;
            break;
        }
    }
    private void createHurtUnluckyMessage(final String source, 
            final String weapon, final int damage) throws RPGException {
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
            StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("The %s sinks into your flesh; %d damage taken.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), damage, source));
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("Moving swifter than you would have thought ");
                sb.append("possible, %s deals you a painful blow. ");
                sb.append("You take %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), source, damage));
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("You rush at the %s but a flurry of blows forces ");
                sb.append("you back; you take %d points of damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), source, damage));
            sb.returnToPool();
            sb = null;
            break;
        }
    }
    private void createHurtLuckyMessage(final String source, 
            final String weapon, final int damage) throws RPGException {
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
            StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("The %s strikes you a glancing blow for %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), source, damage));
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("You dodge most of the %s, but the edge of it ");
                sb.append("catches your ribs for %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), weapon, damage));
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s catches you off-guard, but without much force ");
                sb.append("behind the strike you only take %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), source, damage));
            sb.returnToPool();
            sb = null;
            break;
        }
    }
    private void createHitUnluckyMessage(final String source,
            final String target, final int damage) throws RPGException {
        switch (Diceroller.getInstance().rolldX(3)) {
        case 1:
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("Your attack is off-balance, striking %s ");
                sb.append("without much force; %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), target, damage));
            sb.returnToPool();
            sb = null;
            break;
        case 2:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("%s rolls with your blow, ");
                sb.append("lessening the damage to %d point.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD,
                    String.format(sb.toString(), target, damage));
            sb.returnToPool();
            sb = null;
            break;
        default:
            sb = StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("You swing your %s wildly, landing at an awkward ");
                sb.append("angle; %s takes %d damage.");
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            addMessage(MESSAGE_STANDARD, String.format(sb.toString(),
                    source, target, damage));
            sb.returnToPool();
            sb = null;
            break;
        }
    }
    @Override
    public void doRound() throws RPGException {
        Script.getInstance().setGlobalVariable("COMBATROUND",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND")
                        + 1);
        if (enemies.length > 0) {
            String source, target;
            float damage;
            int msgMode;
            // 1. get creature's Attack Strength.
            FFInteractiveObject enemyIO = enemies[0].getNPCData().getIo();
            FFNpc enemy = enemies[0].getNPCData();
            float enemyAttackStrength = enemy.getFullAttributeScore("SK")
                    + Diceroller.getInstance().rollXdY(2, 6);
            // 2. get player's Attack Strength.
            FFInteractiveObject playerIO =
                    (FFInteractiveObject) Interactive.getInstance().getIO(
                            ProjectConstants.getInstance().getPlayer());
            FFCharacter pc = (FFCharacter) Interactive.getInstance().getIO(
                    ProjectConstants.getInstance().getPlayer()).getPCData();
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
                damage = wpnIO.getItemData().ARX_EQUIPMENT_ComputeDamages(
                        srcIO, trgtIO, luckMod);
                switch (msgMode) {
                case HIT_STD:
                    createHitMessage(target, (int) damage);
                    break;
                case HIT_LUK:
                    createHitLuckMessage(source, target, (int) damage);
                    break;
                case HIT_UNLUK:
                    createHitUnluckyMessage(source, target, (int) damage);
                    break;
                case HURT_STD:
                    createHurtMessage(source, (int) damage);
                    break;
                case HURT_LUK:
                    createHurtLuckyMessage(source, target, (int) damage);
                    break;
                case HURT_UNLUK:
                    createHurtUnluckyMessage(source, target, (int) damage);
                    break;
                }
            } else {
                endRound();
            }
        }
    }
    private void endRound() {
        clearExcessMessages();
    }
    /**
     * Gets the value for the messages.
     * @return {@link String[]}
     */
    public String[] getMessages() {
        return messages;
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
