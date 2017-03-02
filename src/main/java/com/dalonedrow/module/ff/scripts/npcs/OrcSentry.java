package com.dalonedrow.module.ff.scripts.npcs;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.engine.systems.base.Diceroller;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.engine.systems.base.Time;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.module.ff.systems.Combat;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.constants.Behaviour;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.constants.ScriptConsts;
import com.dalonedrow.rpg.base.flyweights.BaseInteractiveObject;
import com.dalonedrow.rpg.base.flyweights.BehaviorParameters;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.ScriptTimerInitializationParameters;
import com.dalonedrow.rpg.base.flyweights.SendParameters;
import com.dalonedrow.rpg.base.flyweights.SpeechParameters;
import com.dalonedrow.rpg.base.flyweights.TargetParameters;
import com.dalonedrow.rpg.base.systems.Script;

/**
 * $: GLOBAL TEXT £: LOCAL TEXT #: GLOBAL LONG §: LOCAL LONG &: GLOBAL
 * FLOAT @:LOCAL FLOAT based on goblin_base.asl
 * @author drau
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class OrcSentry extends FFScriptable {
    /** FIGHTING MODE FLEE. */
    private static final int FM_FLEE = 2;
    /** constant for NO HELPING BUDDY. */
    private static final int HB_NO_BUDDY = -1;
    /** reflection mode: NORMAL. */
    private static final int RM_NORMAL = 1;
    /** reflection mode: NOTHING. */
    private static final int RM_NOTHING = 0;
    /** reflection mode: SEARCH. */
    private static final int RM_SEARCH = 3;
    /** reflection mode: THREAT. */
    private static final int RM_THREAT = 2;
    private static final int TACTIC_CASTER = 3;
    private static final int TACTIC_NORMAL = 0;
    private static final int TACTIC_RABBIT = 2;
    private static final int TACTIC_SNEAK = 1;
    /**
     * Creates a new instance of {@link OrcCleaverScript}.
     * @param io the IO associated with the script
     */
    public OrcSentry(final FFInteractiveObject io) {
        super(io);
    }
    private void attackPlayer() throws RPGException {
        if (Script.getInstance().amISpeaking(super.getIO())) {
            if (getLocalVarEnemy()) {
                Script.getInstance().speak(super.getIO(),
                        new SpeechParameters(null, null));
            }
        }
    }
    private void attackPlayerAfterOuch() throws RPGException {
        setLocalVarIgnoreFailure(false);
        if (Script.getInstance().isPlayerInvisible(super.getIO())) {
            lookForSuite();
        } else {
            // turn off the 'lookfor' timer
            Script.getInstance().timerClearByNameAndIO("lookfor",
                    super.getIO());
            // turn off the 'heard' timer
            Script.getInstance().timerClearByNameAndIO("heard", super.getIO());
            setLocalVarPanicMode(1);
            setLocalVarLookingFor(0);
            setLocalVarEnemy(true);
            // turn off hearing - io is in combat
            super.assignDisallowedEvent(ScriptConsts.DISABLE_HEAR);
            if (getLocalVarNoiseHeard() < 2) {
                setLocalVarNoiseHeard(2);
            }
            callForHelp();
            if (!getLocalVarPlayerEnemySend()) {
                setLocalVarPlayerEnemySend(true);
                // send event to all members of group that the player attacked
                Script.getInstance().sendEvent(super.getIO(),
                        new SendParameters("GROUP", // parameters
                                getLocalVarFriend(), // group name
                                null, // event parameters
                                "onPlayerEnemy", // event name
                                null, // target name
                                0)); // radius
                // System.out.println("PLAYER_ENEMY sent");
            }
            // kill all local timers
            Script.getInstance().timerClearAllLocalsForIO(super.getIO());
            // clear the quiet timer
            Script.getInstance().timerClearByNameAndIO("quiet", super.getIO());
            // SET_NPC_STAT BACKSTAB 0
            if (super.getIO().getNPCData()
                    .getBaseLife() < getLocalVarCowardice()) {
                flee();
            } else if (getLocalVarFightingMode() == FM_FLEE) {
                // running away
            } else {
                if (getLocalVarFightingMode() == 1) {
                    setLocalVarReflectionMode(2);
                } else {
                    if (super.getLocalStringVariableValue(
                            "attached_object") != "NONE") {
                        // DETACH ~£attached_object~ SELF
                        // OBJECT_HIDE ~£attached_object~ ON
                        super.setLocalVariable("attached_object", "NONE");
                    }
                    saveBehavior();
                    // reset misc reflection timer
                    Script.getInstance().stackSendIOScriptEvent(super.getIO(),
                            0, null, "onMiscReflection");
                    if (getLocalVarFightingMode() == 3
                            && super.getIO().getNPCData()
                                    .getBaseLife() < getLocalVarCowardice()) {
                        flee();
                    } else if (super.getLocalIntVariableValue("tactic") == 2) {
                        flee(); // coward
                    } else {
                        if (!getLocalVarSpotted()) {
                            setLocalVarSpotted(true);
                            // hail aggressively
                            // SPEAK -a ~£hail~ NOP
                            Script.getInstance().speak(super.getIO(),
                                    new SpeechParameters("A",
                                            getLocalVarHail()));
                        }
                        setLocalVarReflectionMode(RM_THREAT);
                        super.setLocalVariable("fighting_mode", 1);
                        if (super.getLocalIntVariableValue("tactic") == 0) {
                            super.behavior(new BehaviorParameters("F", 0));
                        } else if (super.getLocalIntVariableValue(
                                "tactic") == 1) {
                            super.behavior(
                                    new BehaviorParameters("S F MOVE_TO", 0));
                        } else if (super.getLocalIntVariableValue(
                                "tactic") == 3) {
                            super.behavior(new BehaviorParameters("M", 0));
                        }
                        // set pathfinding to target player
                        super.setTarget(new TargetParameters("-A PLAYER"));
                        // set weapon in hand
                        // TODO - set weapon
                        super.getIO().getNPCData()
                                .setMovemode(IoGlobals.RUNMODE);
                    }
                }
            }
        }
    }
    private void callForHelp() throws RPGException {
        if (!getLocalVarFriend().equalsIgnoreCase("NONE")) {
            if (!getLocalVarControlsOff()) {
                long tmp = Script.getInstance().getGameSeconds();
                tmp -= getLocalVarLastCallForHelp();
                if (tmp > 4) {
                    // don't call for help too often...
                    // System.out.println("CALL FOR HELP !!!");
                    if (getLocalVarFightingMode() == 2) {
                        // call all friends within 1200 unit radius
                        Script.getInstance().sendEvent(super.getIO(),
                                new SendParameters("GROUP RADIUS",
                                        getLocalVarFriend(), // group name
                                        null, // no event params
                                        "callHelp", // event name
                                        null, // no target
                                        1200));// radius
                    } else {
                        // send call to everyone within 600 unit radius
                        Script.getInstance().sendEvent(super.getIO(),
                                new SendParameters("GROUP RADIUS",
                                        getLocalVarFriend(), // group name
                                        null, // no event params
                                        "callHelp", // event name
                                        null, // no target
                                        600));// radius
                    }
                    setLocalVarLastCallForHelp(
                            Script.getInstance().getGlobalIntVariableValue(
                                    "COMBATROUND"));
                }
            }
        }
    }
    private void checkPlayerDistance() throws RPGException {
        if (super.getLocalIntVariableValue("fighting") != 0) {
            // turn off the 'colplayer' timer
            Script.getInstance().timerClearByNameAndIO("colplayer",
                    super.getIO());
            super.setLocalVariable("collided_player", 0);
            Script.getInstance().setEvent(super.getIO(), "COLLIDE_NPC", true);
            System.out.println("collide npc ON");
        } else {
            // IF DISTANCE TO PLAYER IS GREATER THAN 200 {
            // turn off the 'colplayer' timer
            Script.getInstance().timerClearByNameAndIO("colplayer",
                    super.getIO());
            super.setLocalVariable("collided_player", 0);
            restoreBehavior();
            Script.getInstance().setEvent(super.getIO(), "COLLIDE_NPC", true);
            System.out.println("collide npc ON");
            // }
        }
    }
    private void failedPathfind() throws RPGException {
        System.out.println("pathfail");
        if (!getLocalVarIgnoreFailure()) {
            setLocalVarIgnoreFailure(true);
            super.behavior(new BehaviorParameters("NONE", 0));
            super.setTarget(new TargetParameters("NONE"));
            setLocalVarFightingMode(0);
            setLocalVarReflectionMode(0);
            // TIMERpathfail 1 3 SET §ignorefailure 0 GOTO GO_HOME
            ScriptTimerInitializationParameters<
                    FFInteractiveObject> timerParams =
                            new ScriptTimerInitializationParameters<
                                    FFInteractiveObject>();
            timerParams.setName("pathfail");
            timerParams.setScript(this);
            timerParams.setIo(super.getIO());
            timerParams.setRepeatTimes(1);
            timerParams.setMilliseconds(3000);
            timerParams.setStartTime(Time.getInstance().getGameTime());
            try {
                timerParams.setObj(this);
                timerParams.setMethod(getClass().getMethod("resetPathFail"));
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
            }
            Script.getInstance().startTimer(timerParams);
            timerParams.clear();
            timerParams = null;
        }
    }
    private void flee() throws RPGException {
        if (getLocalVarFightingMode() != FM_FLEE) {
            // change to fleeing
            setLocalVarFightingMode(FM_FLEE);
            System.out.println("Fleeing");
            // kill local timers
            Script.getInstance().timerClearAllLocalsForIO(super.getIO());
            // turn off hearing and collisions - io is fleeing
            super.assignDisallowedEvent(ScriptConsts.DISABLE_HEAR);
            super.assignDisallowedEvent(ScriptConsts.DISABLE_COLLIDE_NPC);
            setLocalVarReflectionMode(RM_NOTHING);
            if (Interactive.getInstance().getTargetByNameTarget(
                    getLocalVarHelpingBuddy()) == HB_NO_BUDDY) {
                super.behavior(new BehaviorParameters("FLEE", 1000));
                // set pathfinding to target player
                super.setTarget(new TargetParameters("PLAYER"));
                super.getIO().getNPCData().setMovemode(IoGlobals.RUNMODE);
                if (Script.getInstance().isIOInGroup(super.getIO(), "UNDEAD")) {
                    super.getIO().getNPCData().setMovemode(IoGlobals.WALKMODE);
                }
            } else {
                Script.getInstance().sendEvent(super.getIO(),
                        new SendParameters(null,
                                getLocalVarFriend(), // group name
                                new Object[] { "flee_marker",
                                        getLocalVarFleeMarker() }, // event pars
                                "onPanic", // event name
                                null, // no target
                                0));// radius
                super.behavior(new BehaviorParameters("MOVE_TO", 0));
                // set pathfinding to target helping buddy
                PooledStringBuilder sb =
                        StringBuilderPool.getInstance().getStringBuilder();
                try {
                    sb.append("-a ID_");
                    sb.append(getLocalVarHelpingBuddy());
                } catch (PooledException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
                }
                super.setTarget(new TargetParameters(sb.toString()));
                sb.returnToPool();
                sb = null;
                super.getIO().getNPCData().setMovemode(IoGlobals.RUNMODE);
                if (Script.getInstance().isIOInGroup(super.getIO(), "UNDEAD")) {
                    super.getIO().getNPCData().setMovemode(IoGlobals.WALKMODE);
                }
            }
            callForHelp();
            // set coward timer
            // TIMERcoward 1 2 SENDEVENT SPEAK_NO_REPEAT SELF "5 A £help"
            ScriptTimerInitializationParameters<
                    FFInteractiveObject> timerParams =
                            new ScriptTimerInitializationParameters<
                                    FFInteractiveObject>();
            timerParams.setName("coward");
            timerParams.setScript(this);
            timerParams.setIo(super.getIO());
            timerParams.setMilliseconds(2000);
            timerParams.setStartTime(Time.getInstance().getGameTime());
            timerParams.setRepeatTimes(1);
            try {
                timerParams.setObj(Script.getInstance());
                timerParams.setMethod(
                        Script.class.getMethod("sendEvent",
                                BaseInteractiveObject.class,
                                SendParameters.class));
                timerParams.setArgs(new Object[] {
                        super.getIO(), new SendParameters(
                                null, // init parameters
                                null, // group name
                                new Object[] { "speech_interval", 5,
                                        "speech_parameters", "A",
                                        "speech_text", getLocalVarHelp() },
                                // event parameterss
                                "onSpeakNoRepeat", // event name
                                "SELF", // no target
                                0) // radius
                });
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
            }
            Script.getInstance().startTimer(timerParams);
            timerParams.clear();

            // set home timer
            // TIMERhome 1 30 GOTO GO_HOME
            timerParams.setName("home");
            timerParams.setScript(this);
            timerParams.setIo(super.getIO());
            timerParams.setMilliseconds(30000);
            timerParams.setStartTime(Time.getInstance().getGameTime());
            timerParams.setRepeatTimes(1);
            try {
                timerParams.setObj(this);
                timerParams.setMethod(getClass().getMethod("goHome"));
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
            }
            timerParams.setRepeatTimes(1);
            Script.getInstance().startTimer(timerParams);
            timerParams = null;
        }
    }
    private void fleeEnd() throws RPGException {
        System.out.println("flee_end");
        if (!getLocalVarControlsOff()) {
            if (getLocalVarFightingMode() == FM_FLEE) {
                setLocalVarFightingMode(3);
                // turn on hearing
                super.removeDisallowedEvent(ScriptConsts.DISABLE_HEAR);
                // turn on collisions
                super.removeDisallowedEvent(ScriptConsts.DISABLE_COLLIDE_NPC);
                setLocalVarCowardice(getLocalVarCowardice() - 2);
                if (getLocalVarCowardice() < 3) {
                    setLocalVarCowardice(0);
                }
                if (getLocalVarPlayerInSight()) {
                    attackPlayer();
                } else {
                    super.behavior(new BehaviorParameters("FRIENDLY", 0));
                    super.setTarget(new TargetParameters("PLAYER"));
                }
            }
        }
    }
    private String getLocalSpeechAggression() throws RPGException {
        return super.getLocalStringVariableValue("sp_aggression");
    }
    private String getLocalSpeechDying() throws RPGException {
        return super.getLocalStringVariableValue("sp_dying");
    }
    private String getLocalSpeechMisc() throws RPGException {
        return super.getLocalStringVariableValue("sp_misc");
    }
    private String getLocalSpeechOuch() throws RPGException {
        return super.getLocalStringVariableValue("sp_ouch");
    }
    private String getLocalSpeechOuchMedium() throws RPGException {
        return super.getLocalStringVariableValue("sp_ouch_medium");
    }
    private String getLocalSpeechOuchStrong() throws RPGException {
        return super.getLocalStringVariableValue("sp_ouch_strong");
    }
    private String getLocalSpeechSearch() throws RPGException {
        return super.getLocalStringVariableValue("sp_search");
    }
    private String getLocalSpeechShortReflection() throws RPGException {
        return (String) Diceroller.getInstance().getRandomObject(
                super.getLocalStringArrayVariableValue(
                        "sp_short_misc_reflections"));
    }
    private String getLocalSpeechThief() throws RPGException {
        return super.getLocalStringVariableValue("sp_thief");
    }
    private String getLocalSpeechThreat() throws RPGException {
        return super.getLocalStringVariableValue("sp_threat");
    }
    private String getLocalSpeechVictory() throws RPGException {
        return super.getLocalStringVariableValue("sp_victory");
    }
    private String getLocalVarBackToGuard() throws RPGException {
        return super.getLocalStringVariableValue("back2guard");
    }
    private int getLocalVarCastingLevel() throws RPGException {
        return super.getLocalIntVariableValue("casting_lvl");
    }
    private String getLocalVarCombatMessage() throws RPGException {
        return super.getLocalStringVariableValue("combat_message");
    }
    private boolean getLocalVarConfused() throws RPGException {
        return super.getLocalIntVariableValue("confused") == 1;
    }
    /**
     * Gets the flag indicating controls are off, and there shouldn't be any
     * reaction to events.
     * @return {@link boolean}
     * @throws RPGException if an error occurs
     */
    private boolean getLocalVarControlsOff() throws RPGException {
        return super.getLocalIntVariableValue("controls_off") == 1;
    }
    private int getLocalVarCowardice() throws RPGException {
        return super.getLocalIntVariableValue("cowardice");
    }
    /**
     * Gets the flag indicating whether the player is the enemy right now.
     * @return <tt>true</tt> if the player is an enemy; <tt>false</tt>otherwise
     * @throws RPGException if an error occurs
     */
    private boolean getLocalVarEnemy() throws RPGException {
        return super.getLocalIntVariableValue("enemy") == 1;
    }
    private int getLocalVarFightingMode() throws RPGException {
        return super.getLocalIntVariableValue("fighting_mode");
    }
    private String getLocalVarFleeMarker() throws RPGException {
        return super.getLocalStringVariableValue("flee_marker");
    }
    private String getLocalVarFriend() throws RPGException {
        return super.getLocalStringVariableValue("friend");
    }
    private String getLocalVarHail() throws RPGException {
        return super.getLocalStringVariableValue("hail");
    }
    private String getLocalVarHelp() throws RPGException {
        return super.getLocalStringVariableValue("help");
    }
    private String getLocalVarHelpingBuddy() throws RPGException {
        return super.getLocalStringVariableValue("helping_buddy");
    }
    private String getLocalVarHelpingTarget() throws RPGException {
        return super.getLocalStringVariableValue("helping_target");
    }
    private boolean getLocalVarIgnoreFailure() throws RPGException {
        return super.getLocalIntVariableValue("ignorefailure") == 1;
    }
    private String getLocalVarJustYouWait() throws RPGException {
        return super.getLocalStringVariableValue("justyouwait");
    }
    /**
     * Gets the last combat round the script called for help.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    private int getLocalVarLastCallForHelp() throws RPGException {
        return super.getLocalIntVariableValue("last_call_help");
    }
    private String getLocalVarLastHeard() throws RPGException {
        return super.getLocalStringVariableValue("last_heard");
    }
    private long getLocalVarLastReflection() throws RPGException {
        return super.getLocalLongVariableValue("last_reflection");
    }
    private int getLocalVarLookingFor() throws RPGException {
        return super.getLocalIntVariableValue("looking_for");
    }
    private int getLocalVarNoiseHeard() throws RPGException {
        return super.getLocalIntVariableValue("noise_heard");
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
     * Gets the last combat round an 'OUCH' event occurred.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    private int getLocalVarOuchTime() throws RPGException {
        return super.getLocalIntVariableValue("ouch_time");
    }
    /**
     * Gets the scripted pain threshold. Damage over this amount increases the
     * chance of a spoken reaction.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    private int getLocalVarPain() throws RPGException {
        return super.getLocalIntVariableValue("pain");
    }
    private int getLocalVarPanicMode() throws RPGException {
        return super.getLocalIntVariableValue("panicmode");
    }
    private boolean getLocalVarPlayerEnemySend() throws RPGException {
        return super.getLocalIntVariableValue("player_enemy_send") == 1;
    }
    private boolean getLocalVarPlayerInSight() throws RPGException {
        return super.getLocalIntVariableValue("player_in_sight") == 1;
    }
    /**
     * Gets the script's reflection mode.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    private int getLocalVarReflectionMode() throws RPGException {
        return super.getLocalIntVariableValue("reflection_mode");
    }
    private int getLocalVarShortReflections() throws RPGException {
        return super.getLocalIntVariableValue("short_reflections");
    }
    /**
     * Gets the flag indicating whether the script's IO is sleeping.
     * @return {@link boolean}
     * @throws RPGException if an error occurs
     */
    private boolean getLocalVarSleeping() throws RPGException {
        return super.getLocalIntVariableValue("sleeping") == 1;
    }
    private boolean getLocalVarSpotted() throws RPGException {
        return super.getLocalIntVariableValue("spotted") == 1;
    }
    private int getLocalVarSummonAttacking() throws RPGException {
        return super.getLocalIntVariableValue("summon_attacking");
    }
    /**
     * Gets the amount of 'SUMMONED OUCH' damage that occurred.
     * @return {@link float}
     * @throws RPGException if an error occurs
     */
    private float getLocalVarSummonedOuch() throws RPGException {
        return super.getLocalFloatVariableValue("SUMMONED_OUCH");
    }
    private int getLocalVarTmpInt1() throws RPGException {
        return super.getLocalIntVariableValue("tmp_int1");
    }
    private String getLocalVarTmpString1() throws RPGException {
        return super.getLocalStringVariableValue("tmp_string1");
    }
    private String getLocalVarTmpString2() throws RPGException {
        return super.getLocalStringVariableValue("tmp_string2");
    }
    private boolean getLocalVarTotaldead() throws RPGException {
        return super.getLocalIntVariableValue("totaldead") == 1;
    }
    private String getLocalVarType() throws RPGException {
        return super.getLocalStringVariableValue("type");
    }
    public void goBackToGuard() throws RPGException {
        Script.getInstance().sendEvent(super.getIO(),
                new SendParameters("", // init parameters
                        null, // group name
                        new Object[] {
                                "speech_interval", 3,
                                "speech_parameters", "N",
                                "speech_text",
                                super.getLocalStringVariableValue("back2guard")
                        }, // event parameters
                        "onSpeakNoRepeat", // event name
                        "SELF", // target name
                        0)); // radius
        restoreBehavior();
    }
    private void goHome() throws RPGException {
        if (!getLocalVarControlsOff()
                && getLocalVarFightingMode() != 1) {
            if (getLocalVarFightingMode() >= 2) {
                setLocalVarFightingMode(3);
                setLocalVarCowardice(getLocalVarCowardice() / 2);
                if (getLocalVarCowardice() < 3) {
                    // next time attack and fight to death !
                    setLocalVarCowardice(0);
                }
                // flee again if player is around the corner
                // IF (^DIST_PLAYER < 500) GOTO ATTACK_PLAYER
            }
            setLocalVarLastHeard("NOHEAR");
            // turn on aggression
            super.removeDisallowedEvent(ScriptConsts.DISABLE_AGGRESSION);
            // turn on hearing
            super.removeDisallowedEvent(ScriptConsts.DISABLE_HEAR);
            setLocalVarFightingMode(0);
            setLocalVarSpotted(false);
            if (getLocalVarLookingFor() == 3) {
                Script.getInstance().sendEvent(super.getIO(),
                        new SendParameters(
                                null, // init parameters
                                null, // group name
                                new Object[] { "speech_interval", 3,
                                        "speech_parameters", "A",
                                        "speech_text",
                                        getLocalVarJustYouWait() },
                                // event parameterss
                                "onSpeakNoRepeat", // event name
                                "SELF", // no target
                                0));
            }
            setLocalVarLookingFor(0);
            setLocalVarReflectionMode(1);
            System.out.println("WEAPON OFF");
            // WEAPON OFF
            restoreBehavior();
            // SET_NPC_STAT BACKSTAB 1
        }
    }
    /**
     * Initializes all local variables.
     * @throws RPGException if an error occurs
     */
    private void initLocalSpeech() throws RPGException {
        setLocalSpeechAggression(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_aggression"));
        setLocalSpeechDying(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_dying"));
        setLocalSpeechMisc("");
        setLocalSpeechOuch(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_ouch"));
        setLocalSpeechOuchMedium(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_ouch_medium"));
        setLocalSpeechOuchStrong(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_ouch_strong"));
        setLocalSpeechSearch(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_search"));
        setLocalSpeechShortReflection(new String[] { "" });
        setLocalSpeechThief(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_thief"));
        setLocalSpeechThreat(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_threat"));
        setLocalSpeechVictory(FFWebServiceClient.getInstance().loadText(
                "orc_sentry_victory"));
    }
    /**
     * Initializes all local variables.
     * @throws RPGException if an error occurs
     */
    private void initLocalVars() throws RPGException {
        setLocalVarCombatMessage("");
        setLocalVarControlsOff(false);
        // if life < cowardice, NPC flees
        setLocalVarCowardice(0);
        // to avoid to many CALL_FOR_HELP events
        setLocalVarLastCallForHelp(0);
        // the last ouch damage to occur
        setLocalVarOuch(0);
        // last combat round an 'OUCH' event occurred
        setLocalVarOuchTime(0);
        // the pain threshold. when damage > pain threshold,
        // the chances of a reaction increase
        setLocalVarPain(3);
        // true : PLAYER_ENEMY event already sent by this NPC
        setLocalVarPlayerEnemySend(false);
        // true indicates that the NPC currently sees he player.
        setLocalVarPlayerInSight(false);
        // 0: nothing, 1: normal, 2: threat, 3: search
        setLocalVarReflectionMode(RM_NOTHING);
        // meynier... tu dors...
        setLocalVarSleeping(true);
        // defines if the NPC has already said "I'll get you" to the player
        setLocalVarSpotted(false);
        // the last 'summoned' ouch damage to occur
        setLocalVarSummonedOuch(0);
    }
    private void lookFor() throws RPGException {
        if (!getLocalVarControlsOff()) {
            // clear the 'lookfor' timer
            Script.getInstance().timerClearByNameAndIO("lookfor",
                    super.getIO());
            if (getLocalVarConfused()
                    || Script.getInstance().isPlayerInvisible(super.getIO())) {
                lookForSuite();
            } else if (Script.getInstance().getGlobalLongVariableValue(
                    "DIST_PLAYER") < 500) {
                playerDetected();
            }
        }
    }
    private void lookForSuite() throws RPGException {
        if (!getLocalVarControlsOff()) {
            if (super.getLocalIntVariableValue("looking_for") > 2) {
                playerDetected();
            } else {
                if (super.getLocalIntVariableValue("fighting_mode") <= 1) {
                    super.behavior(new BehaviorParameters("LOOK_FOR", 500));
                    super.setTarget(new TargetParameters("-a PLAYER"));
                    super.getIO().getNPCData().setMovemode(IoGlobals.WALKMODE);
                    super.setLocalVariable("looking_for", 2);
                    super.setLocalVariable("fighting_mode", 0);
                    super.removeDisallowedEvent(ScriptConsts.DISABLE_HEAR);
                    setLocalVarReflectionMode(OrcSentry.RM_SEARCH);
                    // TIMERhome 1 18 GOTO GO_HOME
                    ScriptTimerInitializationParameters<
                            FFInteractiveObject> timerParams =
                                    new ScriptTimerInitializationParameters<
                                            FFInteractiveObject>();
                    timerParams.setName("home");
                    timerParams.setScript(this);
                    timerParams.setIo(super.getIO());
                    timerParams.setMilliseconds(18000);
                    timerParams.setStartTime(Time.getInstance().getGameTime());
                    timerParams.setRepeatTimes(1);
                    try {
                        timerParams.setObj(this);
                        timerParams.setMethod(getClass().getMethod("goHome"));
                    } catch (NoSuchMethodException | SecurityException e) {
                        throw new RPGException(
                                ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
                    }
                    timerParams.setRepeatTimes(1);
                    Script.getInstance().startTimer(timerParams);
                    timerParams = null;
                }
            }
        }
    }
    public int miscReflection() throws RPGException {
        if (super.getIO().getPoisonLevel() > 0) {
            if (!getLocalVarEnemy()) {
                setLocalVarEnemy(true);
                ouchSuite();
            } else if (getLocalVarReflectionMode() > 0
                    && Script.getInstance()
                            .getGlobalIntVariableValue("SHUT_UP") != 1) {
                int tmp;
                if (getLocalVarReflectionMode() == 2) {
                    // in fighting mode -> more reflections - roll 1d10 + 3
                    tmp = Diceroller.getInstance().rolldXPlusY(10, 3);
                } else {
                    // in fighting mode -> more reflections - roll 1d32 + 5
                    tmp = Diceroller.getInstance().rolldXPlusY(32, 5);
                }
                if (getLocalVarType().contains("undead")) {
                    tmp /= 2;
                }
                // set next reflection timer
                ScriptTimerInitializationParameters<
                        FFInteractiveObject> timerParams =
                                new ScriptTimerInitializationParameters<
                                        FFInteractiveObject>();
                timerParams.setName("misc_reflection");
                timerParams.setScript(this);
                timerParams.setFlagValues(1);
                timerParams.setIo(super.getIO());
                timerParams.setMilliseconds(tmp);
                timerParams.setStartTime(Time.getInstance().getGameTime());
                timerParams.setRepeatTimes(0);
                timerParams.setObj(Script.getInstance());
                try {
                    timerParams.setMethod(
                            Script.class.getMethod("stackSendIOScriptEvent",
                                    new Class[] { BaseInteractiveObject.class,
                                            int.class, Object[].class,
                                            String.class }));
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                            e);
                }
                timerParams.setArgs(new Object[] {
                        super.getIO(), 0, null,
                        "miscReflection"
                });
                Script.getInstance().startTimer(timerParams);
                timerParams = null;

                if (getLocalVarReflectionMode() == 1) {
                    if (super.getLocalStringVariableValue("misc") != null) {
                        if (super.getLocalIntVariableValue(
                                "short_reflections") == 1) {
                            if (Diceroller.getInstance().rolldX(2) == 1) {
                                // SENDEVENT SPEAK_NO_REPEAT SELF "6 N
                                // [Human_male_misc_short]" ACCEPT
                            }
                        } else {
                            // SENDEVENT SPEAK_NO_REPEAT SELF "10 N £misc"
                        }
                    }
                } else if (getLocalVarReflectionMode() == 2
                        && super.getLocalStringVariableValue(
                                "threat") != null) {
                    // SENDEVENT SPEAK_NO_REPEAT SELF "3 A £threat"
                } else if (super.getLocalStringVariableValue(
                        "search") != null) {
                    // SENDEVENT SPEAK_NO_REPEAT SELF "3 N £search"
                }
            }
        }
        return ScriptConsts.ACCEPT;
    }
    @Override
    public int onAggression() throws RPGException {
        // System.out.println("on agression");
        if (getLocalVarType().equalsIgnoreCase("human_guard_ss")) {
            Script.getInstance().setGlobalVariable("DISSIDENT_ENEMY", 1);
        }
        if (getLocalVarSleeping()) {
            setLocalVarSleeping(false);
            Script.getInstance().speak(super.getIO(),
                    new SpeechParameters("", getLocalSpeechAggression()));
        }
        ouchStart();
        return super.onAggression();
    }
    @Override
    public int onAttackPlayer() throws RPGException {
        if (!getLocalVarControlsOff()) {
            setLocalVarSpotted(true);
            attackPlayer();
        }
        return super.onAttackPlayer();
    }
    @Override
    public int onCallHelp() throws RPGException {
        if (!getLocalVarControlsOff()
                && !getLocalVarSleeping()) {
            setLocalVarNoiseHeard(2);
            setLocalVarPanicMode(1);
            if (getLocalVarPlayerInSight()
            /* || ^DIST_PLAYER < 500 */) {
                attackPlayer();
            } else {
                if (getLocalVarFightingMode() == 0) {
                    // kill all local timers
                    Script.getInstance()
                            .timerClearAllLocalsForIO(super.getIO());
                    setLocalVarEnemy(true);
                    setLocalVarReflectionMode(0);
                    // SET £helping_target ^SENDER
                    // this.setLocalVarHelpingTarget(val);
                    saveBehavior();
                    super.behavior(new BehaviorParameters("MOVE_TO", 0));
                    PooledStringBuilder sb =
                            StringBuilderPool.getInstance().getStringBuilder();
                    try {
                        sb.append("-na ");
                        sb.append(getLocalVarHelpingTarget());
                    } catch (PooledException e) {
                        throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
                    }
                    super.setTarget(new TargetParameters(sb.toString()));
                    sb.returnToPool();
                    sb = null;
                    super.getIO().getNPCData().setMovemode(IoGlobals.RUNMODE);
                }
            }
        }
        return super.onCallHelp();
    }
    @Override
    public int onChat() throws RPGException {
        if (getLocalVarEnemy()) {
            Script.getInstance().sendEvent(super.getIO(),
                    new SendParameters("", // init parameters
                            null, // group name
                            null, // event parameters
                            "onDetectPlayer", // event name
                            "SELF", // target name
                            0)); // radius
        }
        return super.onChat();
    }
    @Override
    public int onCheatDie() throws RPGException {
        super.setLocalVariable("friend", "NONE");
        Script.getInstance().forceDeath(super.getIO(), "SELF");
        return super.onCheatDie();
    }
    @Override
    public int onCollideDoor() throws RPGException {
        super.setLocalVariable("targeted_door",
                Script.getInstance().getEventSender().getRefId());
        // 1. SEND EVENT TO OPEN TARGETED DOOR
        // SET $TMP " "
        // SENDEVENT NPC_OPEN £targeted_door ~£key_carried~~$TMP~~§enemy~
        // 2. CREATE TIME TO CLOSE TARGETED DOOR
        // TIMERcloseit 1 4 SENDEVENT NPC_CLOSE £targeted_door ~£key_carried~
        return super.onCollideDoor();
    }
    @Override
    public int onCollideNPC() throws RPGException {
        if (Script.getInstance().getEventSender().getRefId() == ProjectConstants
                .getInstance().getPlayer()) {
            if (!getLocalVarControlsOff()) {
                if (super.getLocalIntVariableValue("fighting_mode") != 2) {
                    // CHECK IF PLAYER STEALTH > 50
                    // IF (^PLAYER_SKILL_STEALTH > 50) {
                    // IF NOT FIGHTING, CALL SCRIPT METHOD TO STEAL
                    if (super.getLocalIntVariableValue("fighting_mode") != 1) {
                        // STEALNPC
                    }
                    // }
                    if (super.getLocalIntVariableValue(
                            "collided_player") != 1) {
                        if (getLocalVarEnemy()) {
                            // CHECK IF PLAYER STEALTH < 80
                            // IF (^PLAYER_SKILL_STEALTH < 80) {
                            playerDetected();
                            // }
                        } else {
                            if (super.getLocalIntVariableValue("frozen") != 1) {
                                if (super.getLocalIntVariableValue(
                                        "main_behavior_stacked") == 0) {
                                    saveBehavior();
                                    super.behavior(new BehaviorParameters(
                                            "FRIENDLY", 0));
                                    // SETTARGET PLAYER
                                    // TIMERcolplayer 0 1 goto checkplayerdist
                                    ScriptTimerInitializationParameters timerParams =
                                            new ScriptTimerInitializationParameters();
                                    timerParams.setName("colplayer");
                                    timerParams.setScript(this);
                                    timerParams.setIo(super.getIO());
                                    timerParams.setMilliseconds(1000);
                                    timerParams.setStartTime(
                                            Time.getInstance().getGameTime());
                                    timerParams.setRepeatTimes(0);
                                    timerParams.setObj(this);
                                    try {
                                        timerParams
                                                .setMethod(getClass().getMethod(
                                                        "checkPlayerDistance"));
                                    } catch (NoSuchMethodException
                                            | SecurityException e) {
                                        throw new RPGException(
                                                ErrorMessage.INTERNAL_BAD_ARGUMENT,
                                                e);
                                    }
                                    Script.getInstance()
                                            .startTimer(timerParams);
                                    timerParams = null;
                                    super.setLocalVariable("collided_player",
                                            1);
                                    Script.getInstance().setEvent(super.getIO(),
                                            "COLLIDE_NPC", false);
                                    System.out.println("collide npc OFF");
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.onCollideNPC();
    }
    @Override
    public int onCollisionError() throws RPGException {
        if (!getLocalVarControlsOff()) {
            System.out.println("collision_error");
            // turn off collisions
            super.assignDisallowedEvent(ScriptConsts.DISABLE_COLLIDE_NPC);
            // set col timer to turn collisions back on after a second
            // TIMERcol 1 1 COLLISION ON
            ScriptTimerInitializationParameters timerParams =
                    new ScriptTimerInitializationParameters();
            timerParams.setName("col");
            timerParams.setScript(this);
            timerParams.setIo(super.getIO());
            timerParams.setMilliseconds(1000);
            timerParams.setStartTime(Time.getInstance().getGameTime());
            timerParams.setRepeatTimes(1);
            timerParams.setObj(this);
            try {
                timerParams.setMethod(getClass()
                        .getMethod("removeDisallowedEvent", int.class));
                timerParams.setArgs(
                        new Object[] { ScriptConsts.DISABLE_COLLIDE_NPC });
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
            }
            Script.getInstance().startTimer(timerParams);
            timerParams = null;
        }
        return super.onCollisionError();
    }
    @Override
    public int onControlsOff() throws RPGException {
        setLocalVarControlsOff(true);
        super.setLocalVariable("saved_reflection", getLocalVarReflectionMode());
        setLocalVarReflectionMode(RM_NOTHING);
        Script.getInstance().setGlobalVariable("SHUT_UP", 1);
        if (getLocalVarEnemy()
                && super.getLocalIntVariableValue("frozen") != 1) {
            // turn collisions off
            super.assignDisallowedEvent(ScriptConsts.DISABLE_COLLIDE_NPC);
            super.behavior(new BehaviorParameters("STACK", 0));
            super.behavior(new BehaviorParameters("FRIENDLY", 0));
            super.setTarget(new TargetParameters("PLAYER"));
        }
        return super.onControlsOn();
    }
    @Override
    public int onControlsOn() throws RPGException {
        if (getLocalVarControlsOff()) {
            setLocalVarControlsOff(false);
            setLocalVarReflectionMode(
                    super.getLocalIntVariableValue("saved_reflection"));
            Script.getInstance().setGlobalVariable("SHUT_UP", 0);
            if (getLocalVarEnemy()
                    && super.getLocalIntVariableValue("frozen") != 1) {
                // turn collisions back on
                super.removeDisallowedEvent(ScriptConsts.DISABLE_COLLIDE_NPC);
                super.behavior(new BehaviorParameters("UNSTACK", 0));
            }
        }
        return super.onControlsOn();
    }
    @Override
    public int onDelation() throws RPGException {
        if (getLocalVarFightingMode() == 0) {
            setLocalVarHelpingTarget(getLocalVarTmpString1());
            setLocalVarNoiseHeard(2);
            if (getLocalVarPlayerInSight()) {
                attackPlayer();
            } else {
                // IF (^DIST_PLAYER < 500) GOTO ATTACK_PLAYER
                saveBehavior();
                setLocalVarReflectionMode(0);
                super.behavior(new BehaviorParameters("MOVE_TO", 0));
                PooledStringBuilder sb =
                        StringBuilderPool.getInstance().getStringBuilder();
                try {
                    sb.append("-a ");
                    sb.append(getLocalVarHelpingTarget());
                } catch (PooledException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
                }
                super.setTarget(new TargetParameters(sb.toString()));
                sb.returnToPool();
                sb = null;
                super.getIO().getNPCData().setMovemode(IoGlobals.RUNMODE);
            }
        }
        return super.onDelation();
    }
    @Override
    public int onDetectPlayer() throws RPGException {
        System.out.println("detect");
        return playerDetected();
    }
    @Override
    public int onDie() throws RPGException {
        // turn off chat
        super.assignDisallowedEvent(ScriptConsts.DISABLE_CHAT);
        callForHelp();
        Script.getInstance().timerClearByNameAndIO("misc_reflection",
                super.getIO());
        Script.getInstance().timerClearByNameAndIO("spell_decision",
                super.getIO());
        // FORCEANIM DIE
        if (Script.getInstance().getGlobalIntVariableValue("SHUT_UP") == 0) {
            Script.getInstance().speak(super.getIO(),
                    new SpeechParameters("", getLocalSpeechDying()));
        }
        // turn off collisions
        super.assignDisallowedEvent(ScriptConsts.DISABLE_COLLIDE_NPC);
        // SETDETECT -1
        setLocalVarTotalDead(true);
        if (getLocalVarType().equalsIgnoreCase("human_ylside")) {
            // TIMERsnd 1 1 PLAY "YLSIDE_DEATH"
            // SPECIALFX YLSIDE_DEATH
            // IF (^MYSPELL_SPEED == 1) SPELLCAST -k SPEED
        } else if (getLocalVarType().equalsIgnoreCase("undead_lich")) {
            // IF (^MYSPELL_RAISE_DEAD == 1) SPELLCAST -k RAISE_DEAD
        } else if (getLocalVarType().equalsIgnoreCase("ratmen")) {
            // IF (§stolen_gold == 0) ACCEPT
            // INVENTORY ADDMULTI "Jewelry\\Gold_coin\\Gold_coin" ~§stolen_gold~
        }
        return super.onDie();
    }
    @Override
    public int onDoorLocked() throws RPGException {
        // clear the closeit timer
        Script.getInstance().timerClearByNameAndIO("closeit", super.getIO());
        if (super.getLocalIntVariableValue("door_locked_attempt") < 2) {
            super.setLocalVariable("door_locked_attempt",
                    super.getLocalIntVariableValue("door_locked_attempt") + 1);
        } else {
            super.setLocalVariable("door_locked_attempt", 0);
            failedPathfind();
        }
        return super.onDoorLocked();
    }
    @Override
    public int onFleeEnd() throws RPGException {
        fleeEnd();
        return super.onFleeEnd();
    }
    @Override
    public int onGameReady() throws RPGException {
        // turn collisions back on
        super.removeDisallowedEvent(ScriptConsts.DISABLE_COLLIDE_NPC);
        if (super.getLocalIntVariableValue("casting_lvl") > 0) {
            Script.getInstance().setGlobalVariable("NEGATE", 0);
        }
        if (Script.getInstance().isIOInGroup(super.getIO(), "UNDEAD")) {
            super.setLocalVariable("paralplayer", 0);
            Script.getInstance().setGlobalVariable("REPEL", 0);
        }
        if ("human_priest_highfelnor".contains(
                super.getLocalStringVariableValue("type").toLowerCase())) {
            // for priests healing too much
            super.setLocalVariable("freehealing", 0);
        }
        return super.onGameReady();
    }
    private void orcSentryOnHear() throws RPGException {
        // test player's luck
        FFInteractiveObject plyrIO = ((FFController)
                FFController.getInstance()).getPlayerIO();
        if (!plyrIO.getPCData().testYourLuck(false)) {
            // wake up the orc
            if (getLocalVarSleeping()) {
                setLocalVarSleeping(false);
            }
            System.out.println("start combat");
            // put the player in combat.
            Combat.getInstance().addEnemy(super.getIO());
        }
    }
    @Override
    public int onHear() throws RPGException {
        String name = new String(super.getIO().getNPCData().getName());
        if ("ORC_SENTRY".equalsIgnoreCase(name)) {
            orcSentryOnHear();
        } else {
        if (!getLocalVarControlsOff()
                && !getLocalVarConfused()) {
            // if no noise during 2 minutes, reinit the ON HEAR
            long tmp = Script.getInstance().getGameSeconds();
            tmp -= super.getLocalLongVariableValue("snd_tim");
            if (tmp > 120) {
                setLocalVarNoiseHeard(2);
                super.setLocalVariable("noise_heard", 2);
                super.setLocalVariable("snd_tim",
                        Script.getInstance().getGlobalLongVariableValue(
                                "GAMESECONDS"));
            }
            if (getLocalVarSleeping()) {
                setLocalVarSleeping(false);
            } else {
                boolean gotoAccept = false;
                if (getLocalVarEnemy()
                        && super.getLocalIntVariableValue("force_hear") == 0) {
                    gotoAccept = true;
                }
                if (!gotoAccept
                        && !getLocalVarPlayerInSight()
                        && super.getLocalIntVariableValue("panicmode") != 2) {
                    if (super.getLocalIntVariableValue("looking_for") >= 1) {
                        attackPlayer();
                    } else if (super.getLocalIntVariableValue(
                            "fighting_mode") >= 1) {
                        playerDetected();
                    } else {
                        if (Script.getInstance().getEventSender()
                                .getRefId() == ProjectConstants.getInstance()
                                        .getPlayer()) {
                            // IF (^PLAYERSPELL_INVISIBILITY == 1) {
                            lookForSuite();
                            // }
                        } else if (Script.getInstance().getEventSender()
                                .getRefId() == super.getLocalIntVariableValue(
                                        "last_heard")) {
                            tmp = Script.getInstance()
                                    .getGlobalLongVariableValue(
                                            "GAMESECONDS");
                            tmp -= super.getLocalLongVariableValue("snd_tim");
                            if (tmp < 2) {
                                gotoAccept = true;
                            }
                        }
                        if (!gotoAccept) {
                            // turn off the 'heard' timer
                            Script.getInstance().timerClearByNameAndIO("heard",
                                    super.getIO());
                            super.setLocalVariable("noise_heard",
                                    super.getLocalIntVariableValue(
                                            "noise_heard") + 1);
                            if (super.getLocalIntVariableValue(
                                    "noise_heard") > 3) {
                                playerDetected();
                            } else {
                                super.setLocalVariable("last_heard",
                                        Script.getInstance().getEventSender()
                                                .getRefId());
                                super.setLocalVariable("snd_tim",
                                        Script.getInstance()
                                                .getGlobalLongVariableValue(
                                                        "GAMESECONDS"));
                                if (super.getLocalIntVariableValue(
                                        "panicmode") != 2) {
                                    super.setLocalVariable("panicmode", 1);
                                }
                                setLocalVarReflectionMode(RM_NOTHING);
                                // turn off the 'quiet' timer
                                Script.getInstance().timerClearByNameAndIO(
                                        "quiet",
                                        super.getIO());
                                saveBehavior();
                                if (Diceroller.getInstance().rolldX(2) == 1) {
                                    Script.getInstance().speak(super.getIO(),
                                            new SpeechParameters("A",
                                                    super.getLocalStringVariableValue(
                                                            "heardnoise")));
                                }
                                if (super.getLocalIntVariableValue(
                                        "noise_heard") == 1) {
                                    // BEHAVIOR NONE
                                    super.behavior(
                                            new BehaviorParameters("NONE", 0));
                                    super.setTarget(
                                            new TargetParameters("NONE"));
                                    super.behavior(
                                            new BehaviorParameters("FRIENDLY",
                                                    0));
                                    BaseInteractiveObject senderIO =
                                            Script.getInstance()
                                                    .getEventSender();
                                    if (senderIO
                                            .hasIOFlag(IoGlobals.IO_01_PC)) {
                                        super.setTarget(
                                                new TargetParameters("PLAYER"));
                                    } else if (senderIO
                                            .hasIOFlag(IoGlobals.IO_03_NPC)) {
                                        super.setTarget(
                                                new TargetParameters(new String(
                                                        senderIO.getNPCData()
                                                                .getName())));
                                    } else if (senderIO
                                            .hasIOFlag(IoGlobals.IO_02_ITEM)) {
                                        super.setTarget(
                                                new TargetParameters(new String(
                                                        senderIO.getItemData()
                                                                .getItemName())));
                                    }
                                    System.out.println(
                                            "turning to face the sound");
                                    // TIMERheard 0 1 goto checkplayerdist
                                    ScriptTimerInitializationParameters timerParams =
                                            new ScriptTimerInitializationParameters();
                                    timerParams.setName("colplayer");
                                    timerParams.setScript(this);
                                    timerParams.setIo(super.getIO());
                                    timerParams.setMilliseconds(6000);
                                    timerParams.setStartTime(
                                            Time.getInstance().getGameTime());
                                    timerParams.setRepeatTimes(1);
                                    timerParams.setObj(this);
                                    try {
                                        timerParams.setMethod(getClass()
                                                .getMethod("goBackToGuard"));
                                    } catch (NoSuchMethodException
                                            | SecurityException e) {
                                        throw new RPGException(
                                                ErrorMessage.INTERNAL_BAD_ARGUMENT,
                                                e);
                                    }
                                    Script.getInstance()
                                            .startTimer(timerParams);
                                    timerParams = null;
                                } else {
                                    // here we have a new sound source -> go to
                                    // see what it was
                                    System.out.println("going to sound");
                                    super.behavior(
                                            new BehaviorParameters("MOVE_TO",
                                                    0));
                                    PooledStringBuilder sb = StringBuilderPool
                                            .getInstance().getStringBuilder();
                                    try {
                                        sb.append("-n OBJECT_");
                                        sb.append(
                                                super.getLocalStringVariableValue(
                                                        "last_heard"));
                                    } catch (PooledException e) {
                                        throw new RPGException(
                                                ErrorMessage.INTERNAL_ERROR, e);
                                    }
                                    super.setTarget(new TargetParameters(
                                            sb.toString()));
                                    sb.returnToPool();
                                    sb = null;
                                    super.getIO().getNPCData()
                                            .setMovemode(IoGlobals.WALKMODE);
                                }
                            }
                        }
                    }
                }
            }
        }
        }
        name = null;
        return super.onHear();
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onInit()
     */
    @Override
    public int onInit() throws RPGException {
        super.setLocalVariable("voice", "");
        // turn off hearing
        super.assignDisallowedEvent(ScriptConsts.DISABLE_HEAR);
        // initialize local variables
        initLocalVars();
        initLocalSpeech();
        // name of attached object (if one)
        super.setLocalVariable("attached_object", "NONE");
        // if 1 : must have a SPECIAL_ATTACK in
        // the code (ratmen & mummies for instance)
        super.setLocalVariable("special_attack", 0);
        // when = 1, attack mice
        super.setLocalVariable("care_about_mice", 0);

        // in order to restore the main behavior after a look_for or a help
        super.setLocalVariable("main_behavior_stacked", 0);
        // used for various reasons,
        // 1 indicates that the NPC currently sees he

        // if a npc hears a sound more than 3 times, he detects the player
        super.setLocalVariable("noise_heard", 0);
        // 1 = the NPC is about to look for the player 2=looking for him
        super.setLocalVariable("looking_for", 0);
        // 0 = NO 1 = Fighting 2 = Fleeing
        super.setLocalVariable("fighting_mode", 0);
        super.setLocalVariable("last_heard", "NOHEAR");
        super.setLocalVariable("snd_tim", 0);
        super.setLocalVariable("ouch_tim", 0);
        // used for chats to save current reflection_mode
        super.setLocalVariable("saved_reflection", 0);
        // to stop looping anims if ATTACK_PLAYER called
        super.setLocalVariable("frozen", 0);
        // defines the current dialogue position
        super.setLocalVariable("chatpos", 0);
        // current target
        super.setLocalVariable("targ", 0);
        // this stores the name of the current attacked mice, so that the NPC
        // doesn't attack another one until this one is dead.
        super.setLocalVariable("targeted_mice", "NOMOUSE");
        // this stores the name of the current NPC that his being helped.
        super.setLocalVariable("helping_target", "NOFRIEND");
        // this might change, but it currently defines the ONLY
        // key that the NPC carries with them
        super.setLocalVariable("key_carried", "NOKEY");
        // this is used to check what door is dealing the npc with right now.
        super.setLocalVariable("targeted_door", "NODOOR");
        // set backstab stat to 1
        // SET_NPC_STAT BACKSTAB 1
        // only for spell casters
        super.setLocalVariable("spell_ready", 1);
        // friend to run to in case of trouble
        super.setLocalVariable("helping_buddy", "NOBUDDY");
        // last time someone spoke
        super.setLocalVariable("last_reflection", 0l);
        // go back to this marker if combat finished
        super.setLocalVariable("init_marker", "NONE");
        super.setLocalVariable("friend", "NONE");
        // set detection value, from -1 (off) to 100
        // SETDETECT 40
        // the number of attempt at passing a locked door
        super.setLocalVariable("door_locked_attempt", 0);
        // set the radius for physics
        // PHYSICAL RADIUS 30
        // set material
        // SET_MATERIAL FLESH
        // SET_ARMOR_MATERIAL LEATHER
        // SET_STEP_MATERIAL Foot_bare
        // SET_BLOOD 0.9 0.1 0.1
        // inventory created as part of IO
        // SETIRCOLOR 0.8 0.0 0.0
        // stats are set during serialization
        // SET_NPC_STAT RESISTMAGIC 10
        // SET_NPC_STAT RESISTPOISON 10
        // SET_NPC_STAT RESISTFIRE 1
        // defines if the NPC is enemy to the player at the moment
        setLocalVarEnemy(false);
        // when = 0, the NPC is not sure if he saw the
        // player "did that thing move over there ?"
        super.setLocalVariable("panicmode", 0);
        // 0 = normal 1 = sneak 2 = rabit 3 = caster
        super.setLocalVariable("tactic", TACTIC_NORMAL);
        // used to restore previous tactic after a repel undead
        super.setLocalVariable("current_tactic", 0);
        // level of magic needed to confuse this npc
        super.setLocalVariable("confusability", 3);
        // new set the value for the npc heals himself
        super.setLocalVariable("low_life_alert", 1);
        super.setLocalVariable("friend", "goblin");
        super.setLocalVariable("type", "goblin_base");

        return super.onInit();
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onInitEnd()
     */
    @Override
    public int onInitEnd() throws RPGException {
        System.out.println("onInitEnd OrcSentry");
        if (getLocalVarEnemy()) {
            // turn hearing back on
            super.removeDisallowedEvent(ScriptConsts.DISABLE_HEAR);
        }
        if (!"none".equalsIgnoreCase(
                super.getLocalStringVariableValue("friend"))) {
            Script.getInstance().addToGroup(super.getIO(),
                    super.getLocalStringVariableValue("friend"));
        }
        int scale = 95;
        scale += Diceroller.getInstance().rolldX(10);
        super.setLocalVariable("scale", scale);
        // SETSCALE §scale
        if (super.getLocalIntVariableValue("care_about_mice") == 1) {
            Script.getInstance().addToGroup(super.getIO(), "MICECARE");
        }
        if ("goblin_base".equalsIgnoreCase(
                super.getLocalStringVariableValue("type"))) {
            // set localized name
            // SETNAME [description_goblin]

            // set weapon
            Interactive.getInstance().prepareSetWeapon(getIO(), "Orc Cleaver");
            // TODO set additional stats

            // TODO load animations

            // TODO set reflection texts

            // set reflection timer
            ScriptTimerInitializationParameters<
                    FFInteractiveObject> timerParams =
                            new ScriptTimerInitializationParameters<
                                    FFInteractiveObject>();
            timerParams.setName("misc_reflection");
            timerParams.setScript(this);
            timerParams.setFlagValues(1);
            timerParams.setIo(super.getIO());
            timerParams.setMilliseconds(10000);
            timerParams.setStartTime(Time.getInstance().getGameTime());
            timerParams.setRepeatTimes(0);
            timerParams.setObj(Script.getInstance());
            try {
                timerParams.setMethod(
                        Script.class.getMethod("stackSendIOScriptEvent",
                                new Class[] { BaseInteractiveObject.class,
                                        int.class, Object[].class,
                                        String.class }));
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
            }
            timerParams.setArgs(new Object[] {
                    super.getIO(), 0, null,
                    "miscReflection"
            });
            Script.getInstance().startTimer(timerParams);
            timerParams = null;
        }
        String name = new String(super.getIO().getNPCData().getName());
        if ("ORC_SENTRY".equalsIgnoreCase(name)) {
            super.getIO().setPosition(new SimpleVector2(636, 1338));
        }
        name = null;
        return super.onInitEnd();
    }
    @Override
    public int onLookFor() throws RPGException {
        if (!getLocalVarControlsOff()) {
            lookFor();
        }
        return super.onLookFor();
    }
    @Override
    public int onLookMe() throws RPGException {
        if (getLocalVarTmpString2().equalsIgnoreCase("a")) {
            super.behavior(new BehaviorParameters("A FRIENDLY", 0));
        } else {
            super.behavior(new BehaviorParameters("FRIENDLY", 0));
        }
        if (getLocalVarTmpString1().equalsIgnoreCase("PLAYER")) {
            super.setTarget(new TargetParameters("PLAYER"));
        } else {
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("ID_");
                sb.append(Script.getInstance().getEventSender().getRefId());
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            super.setTarget(new TargetParameters(sb.toString()));
            sb.returnToPool();
            sb = null;
        }
        return super.onLookMe();
    }
    @Override
    public int onMiscReflection() throws RPGException {
        if (super.getIO().getNPCData().getPoisonned() > 0
                && !getLocalVarEnemy()) {
            setLocalVarEnemy(true);
            ouchSuite();
        } else if (getLocalVarReflectionMode() != 0
                && Script.getInstance()
                        .getGlobalIntVariableValue("SHUT_UP") != 1) {
            int tmp;
            if (getLocalVarReflectionMode() == 2) {
                // in fighting mode -> more reflections
                tmp = Diceroller.getInstance().rolldXPlusY(10, 3);
            } else {
                tmp = Diceroller.getInstance().rolldXPlusY(32, 5);
            }
            if (super.getIO().isInGroup("UNDEAD")) {
                tmp /= 2;
            }
            // set misc_reflection timer
            // TIMERmisc_reflection -i 0 ~#TMP~ SENDEVENT MISC_REFLECTION SELF
            ScriptTimerInitializationParameters<
                    FFInteractiveObject> timerParams =
                            new ScriptTimerInitializationParameters<
                                    FFInteractiveObject>();
            timerParams.setName("misc_reflection");
            timerParams.setScript(this);
            timerParams.setIo(super.getIO());
            timerParams.setFlagValues(1); // for -i flag
            timerParams.setStartTime(Time.getInstance().getGameTime());
            timerParams.setRepeatTimes(0);
            timerParams.setMilliseconds(tmp * 1000);
            try {
                timerParams.setObj(Script.getInstance());
                timerParams.setMethod(
                        Script.class.getMethod("sendEvent",
                                BaseInteractiveObject.class,
                                SendParameters.class));
                timerParams.setArgs(new Object[] {
                        super.getIO(), new SendParameters(
                                null, // init parameters
                                null, // group name
                                null, // event parameterss
                                "onMiscReflection", // event name
                                "SELF", // no target
                                0) // radius
                });
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
            }
            Script.getInstance().startTimer(timerParams);
            timerParams = null;
            if (getLocalVarReflectionMode() == 1
                    && getLocalSpeechMisc().length() > 0) {
                if (getLocalVarShortReflections() == 1) {
                    if (Diceroller.getInstance().rolldX(2) == 1) {
                        Script.getInstance().sendEvent(super.getIO(),
                                new SendParameters(
                                        null, // init parameters
                                        null, // group name
                                        new Object[] { "speech_interval", 6,
                                                "speech_parameters", "N",
                                                "speech_text",
                                                getLocalSpeechShortReflection() },
                                        // event parameterss
                                        "onSpeakNoRepeat", // event name
                                        "SELF", // no target
                                        0)); // radius
                    }
                } else {
                    Script.getInstance().sendEvent(super.getIO(),
                            new SendParameters(
                                    null, // init parameters
                                    null, // group name
                                    new Object[] { "speech_interval", 10,
                                            "speech_parameters", "N",
                                            "speech_text",
                                            getLocalSpeechMisc() },
                                    // event parameterss
                                    "onSpeakNoRepeat", // event name
                                    "SELF", // no target
                                    0) // radius
                    );
                }
            }
        } else {
            if (getLocalVarReflectionMode() == 2) {
                if (getLocalSpeechThreat().length() > 0) {
                    Script.getInstance().sendEvent(super.getIO(),
                            new SendParameters(
                                    null, // init parameters
                                    null, // group name
                                    new Object[] { "speech_interval", 3,
                                            "speech_parameters", "A",
                                            "speech_text",
                                            getLocalSpeechThreat() },
                                    // event parameterss
                                    "onSpeakNoRepeat", // event name
                                    "SELF", // no target
                                    0) // radius
                    );
                }
            } else {
                if (getLocalSpeechSearch().length() > 0) {
                    Script.getInstance().sendEvent(super.getIO(),
                            new SendParameters(
                                    null, // init parameters
                                    null, // group name
                                    new Object[] { "speech_interval", 3,
                                            "speech_parameters", "N",
                                            "speech_text",
                                            getLocalSpeechSearch() },
                                    // event parameterss
                                    "onSpeakNoRepeat", // event name
                                    "SELF", // no target
                                    0) // radius
                    );
                }
            }
        }
        return super.onMiscReflection();
    }
    @Override
    public int onOtherReflection() throws RPGException {
        // someone has spoken
        setLocalVarLastReflection(Script.getInstance().getGameSeconds());
        return super.onOtherReflection();
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onOuch()
     */
    @Override
    public int onOuch() throws RPGException {
        // System.out.println("on ouch");
        ouchStart();
        ouchSuite();
        return ScriptConsts.ACCEPT;
    }
    @Override
    public int onPathfinderFailure() throws RPGException {
        failedPathfind();
        return super.onPathfinderFailure();
    }
    @Override
    public int onPlayerEnemy() throws RPGException {
        System.out.println("PLAYER_ENEMY received");
        // player is the enemy of this group so don't send this event again !
        setLocalVarPlayerEnemySend(true);
        setLocalVarEnemy(true);
        // turn chat off
        super.assignDisallowedEvent(ScriptConsts.DISABLE_CHAT);
        // turn hearing on
        super.removeDisallowedEvent(ScriptConsts.DISABLE_HEAR);
        if (getLocalVarPlayerInSight()) {
            attackPlayer();
        }
        // IF (^DIST_PLAYER < 500) GOTO ATTACK_PLAYER
        return super.onPlayerEnemy();
    }
    @Override
    public int onReachedTarget() throws RPGException {
        if (super.getIO().getTargetinfo() == ProjectConstants.getInstance()
                .getPlayer()) {
            tryingToReachPlayer();
        } else {
            if (getLocalVarFriend().equalsIgnoreCase("DEMON")
                    && super.getLocalIntVariableValue("order") == 1) {
                super.behavior(new BehaviorParameters("FRIENDLY", 0));
                super.setTarget(new TargetParameters("PLAYER"));
            }
            if (super.getIO().getTargetinfo() == super.getLocalIntVariableValue(
                    "helping_target")
                    && !getLocalVarControlsOff()) {
                // if attacker is dead, go home
                // if attacker is not dead,
                callForHelp();
                lookForSuite();
            } else {
                reachedSound();
            }
        }
        return super.onReachedTarget();
    }
    @Override
    public int onReload() throws RPGException {
        if (Script.getInstance().isIOInGroup(super.getIO(), "KINGDOM")) {
            if (Script.getInstance().getGlobalIntVariableValue(
                    "PLAYER_ON_QUEST") == 6) {
                setLocalVarReflectionMode(RM_NOTHING);
                Script.getInstance().objectHide(
                        super.getIO(), false, "SELF", true);
            } else if (Script.getInstance().getGlobalIntVariableValue(
                    "PLAYER_ON_QUEST") == 7) {
                setLocalVarReflectionMode(RM_NORMAL);
                Script.getInstance().objectHide(
                        super.getIO(), false, "SELF", false);
            }
        } else if (super.isType("human_guard_ss")) {
            if (Script.getInstance()
                    .getGlobalIntVariableValue("DISSIDENT_ENEMY") == 1) {
                setLocalVarEnemy(true);
                if (Script.getInstance()
                        .getGlobalIntVariableValue("weapon_enchanted") == 1) {
                    if (super.getLocalIntVariableValue("dead") != 1) {
                        // teleport to dying marker
                        Script.getInstance().teleport(super.getIO(),
                                false,
                                false,
                                false,
                                super.getLocalStringVariableValue(
                                        "dying_marker"));
                        super.setLocalVariable("dying", "");
                        Script.getInstance().removeGroup(super.getIO(),
                                super.getLocalStringVariableValue("friend"));
                        super.setLocalVariable("friend", "none");
                        Script.getInstance().forceDeath(super.getIO(), "self");
                    }
                }
                if (getLocalVarEnemy()) {
                    if (Script.getInstance().getGlobalIntVariableValue(
                            "weapon_enchanted") == 2) {
                        Script.getInstance().objectHide(
                                super.getIO(), true, "self", true);
                    }
                }
            }
        } else if (!getLocalVarTotaldead()
                && getLocalVarFightingMode() > 0) {
            if (super.getLocalStringVariableValue("init_marker")
                    .equalsIgnoreCase("NONE")) {
                // teleport to initial position
                Script.getInstance().teleport(
                        super.getIO(), false, false, true, null);
            } else {
                // teleport to init marker
                Script.getInstance().teleport(
                        super.getIO(), false, false, false,
                        super.getLocalStringVariableValue("init_marker"));
            }
            // WEAPON OFF
            setLocalVarFightingMode(0);
            setLocalVarPlayerInSight(false);
            setLocalVarReflectionMode(RM_NOTHING);
            // turn hearing back on
            super.removeDisallowedEvent(ScriptConsts.DISABLE_HEAR);
            super.getIO().getNPCData().setMovemode(IoGlobals.WALKMODE);
            super.behavior(new BehaviorParameters("WANDER_AROUND", 300));
            super.setTarget(new TargetParameters("NONE"));
        }
        return super.onReload();
    }
    @Override
    public int onSpeakNoRepeat() throws RPGException {
        if (Script.getInstance().amISpeaking(super.getIO())
                && Script.getInstance().getGlobalIntVariableValue(
                        "SHUT_UP") != 1) {
            // test to see if someone else has spoken recently...
            long tmp = Script.getInstance().getGameSeconds();
            tmp -= super.getLocalLongVariableValue("last_reflection");
            if (tmp > super.getLocalLongVariableValue("speech_interval")) {
                // at least ^#PARAM1 seconds between reflections
                if (super.getLocalStringVariableValue(
                        "speech_parameters").equalsIgnoreCase("N")) {
                    // no switch
                    Script.getInstance().speak(super.getIO(),
                            new SpeechParameters("",
                                    super.getLocalStringVariableValue(
                                            "speech_text")));
                } else {
                    Script.getInstance().speak(super.getIO(),
                            new SpeechParameters(
                                    super.getLocalStringVariableValue(
                                            "speech_parameters"),
                                    super.getLocalStringVariableValue(
                                            "speech_text")));
                }
                // inform other NPC that I have spoken
                Script.getInstance().sendEvent(super.getIO(),
                        new SendParameters("RADIUS NPC", // parameters
                                null, // group name
                                null, // event parameters
                                "onOtherReflection", // event name
                                null, // target name
                                1000)); // radius
                // SENDEVENT -rn OTHER_REFLECTION 1000 ""
                super.setLocalVariable("last_reflection",
                        Script.getInstance().getGameSeconds());
            }
        }
        return super.onSpeakNoRepeat();
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onInit()
     */
    @Override
    public int onSpellcast() throws RPGException {
        // this script performs several checks.
        // 1. is it global SHUT_UP? no spells can be cast during that. ignore
        // 2. is the caster the player? ignore NPCs casting spells.

        // SHUT_UP global var means a cinematic is playing - no spells cast
        if (Script.getInstance().getGlobalIntVariableValue("SHUT_UP") == 0) {
            // not global SHUT_UP. continue processing
            if (Script.getInstance().getEventSender().hasIOFlag(
                    IoGlobals.IO_01_PC)) {
                // caster was a player. continue processing
                if (super.getLocalIntVariableValue("casting_lvl") > 0
                        && Script.getInstance().getGlobalStringVariableValue(
                                "PARAM1_STRING")
                                .equalsIgnoreCase("NEGATE_MAGIC")) {
                    // NEGATE MAGIC SPELL CAST - set global negate
                    Script.getInstance().setGlobalVariable("NEGATE",
                            Script.getInstance().getGlobalIntVariableValue(
                                    "PARAM2_INT"));
                }
            }
        }
        // if caster is not PC - no spells cast
        if (Script.getInstance().getGlobalIntVariableValue("SHUT_UP") != 0
                && Script.getInstance().getEventSender()
                        .hasIOFlag(IoGlobals.IO_01_PC)) {
            /*
             * IF (§casting_lvl != 0) { IF (^$PARAM1 == NEGATE_MAGIC) { SET
             * #NEGATE ^#PARAM2 ACCEPT } } IF ( SELF ISGROUP UNDEAD ) { IF
             * (^$PARAM1 == REPEL_UNDEAD) { SET #REPEL ^#PARAM2 IF (£type ==
             * "undead_lich") { IF (^#PARAM2 < 6) { HERO_SAY -d
             * "pas assez fort, mon fils" ACCEPT } } GOTO REPEL } } IF (^$PARAM1
             * == CONFUSE) { IF (^#PARAM2 < §confusability) ACCEPT SENDEVENT
             * UNDETECTPLAYER SELF "" SET §confused 1 ACCEPT } IF (§enemy == 0)
             * ACCEPT IF (£type == "human_ylside") ACCEPT IF (£type ==
             * "undead_lich") ACCEPT IF (^$PARAM1 == HARM) { IF (^PLAYER_LIFE <
             * 20) ACCEPT GOTO NO_PAIN_REPEL } IF (^$PARAM1 == LIFE_DRAIN) { IF
             * (^PLAYER_LIFE < 20) ACCEPT GOTO NO_PAIN_REPEL } IF (^$PARAM1 ==
             * MANA_DRAIN) { IF (§casting_lvl == 0) ACCEPT IF (^PLAYER_LIFE <
             * 20) ACCEPT GOTO NO_PAIN_REPEL } ACCEPT
             */
        }
        return super.onSpellcast();
    }
    @Override
    public int onSpellEnd() throws RPGException {
        if (Script.getInstance().getEventSender().getRefId() == ProjectConstants
                .getInstance().getPlayer()) {
            if (getLocalVarCastingLevel() != 0) {
                // IF (^$PARAM1 == NEGATE_MAGIC) {
                // SET #NEGATE 0
                // ACCEPT
            }
            // }
            // IF ( SELF ISGROUP UNDEAD )
            // {
            // IF (^$PARAM1 == REPEL_UNDEAD) {
            // SET #REPEL 0
            // }
            // }
            // IF (^$PARAM1 == CONFUSE) {
            // SET §confused 0
            // ACCEPT
            // }
            // IF (§enemy == 0) ACCEPT
            // IF (§fighting_mode != 2) {
            // SET §tactic §current_tactic
            // ACCEPT
            // }
            // IF (£type == "human_ylside") ACCEPT
            // IF (£type == "undead_lich") ACCEPT
            // IF (^$PARAM1 == HARM) GOTO END_PAIN_REPEL
            // IF (^$PARAM1 == LIFE_DRAIN) GOTO END_PAIN_REPEL
            // IF (^$PARAM1 == MANA_DRAIN) GOTO END_REPEL
        }
        return super.onSpellEnd();
    }
    @Override
    public int onSteal() throws RPGException {
        if (getLocalVarTmpString1().equalsIgnoreCase("OFF")) {
            // turn off the 'steal' timer
            Script.getInstance().timerClearByNameAndIO("steal", super.getIO());

        } else if (!getLocalVarControlsOff()
                && getLocalVarPlayerInSight()) {
            Script.getInstance().speak(super.getIO(), new SpeechParameters("A",
                    getLocalSpeechThief()));
            // set steal timer
            // TIMERsteal 1 2 GOTO ATTACK_PLAYER
            ScriptTimerInitializationParameters<
                    FFInteractiveObject> timerParams =
                            new ScriptTimerInitializationParameters<
                                    FFInteractiveObject>();
            timerParams.setName("steal");
            timerParams.setScript(this);
            timerParams.setIo(super.getIO());
            timerParams.setRepeatTimes(1);
            timerParams.setMilliseconds(2000);
            timerParams.setStartTime(Time.getInstance().getGameTime());
            try {
                timerParams.setObj(this);
                timerParams.setMethod(getClass().getMethod("attackPlayer"));
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
            }
            Script.getInstance().startTimer(timerParams);
            timerParams.clear();
            timerParams = null;
        }
        return super.onSteal();
    }
    @Override
    public int onStrike() throws RPGException {
        if (super.getLocalStringVariableValue("type")
                .equalsIgnoreCase("ratmen")) {
            // sample script has ratmen steal gold every 1 out of 4 times they
            // hit
            if (Diceroller.getInstance().rolldX(4) == 4) {
                int tmp = Diceroller.getInstance().rolldXPlusY(10, -1);
                if (tmp > 0) {
                    // if ( #TMP > ^PLAYER_GOLD ) {
                    // DIV #TMP 2
                    // GOTO TEST_PLAYER_GOLD
                }

                // ADD_GOLD -~#TMP~
                // INC §stolen_gold #TMP
                // HERO_SAY -d "le ratman a vole"
                // HERO_SAY -d ~#TMP~
            } else {
                System.out.println("le ratman n'a rien vole");
            }
        }
        if (super.getLocalIntVariableValue("special_attack") == 1) {
            // not defined here
            // specialAttack();
        }
        if (!Script.getInstance().amISpeaking(super.getIO())) {
            if (Diceroller.getInstance().rolldX(2) == 1) {
                Script.getInstance().speak(super.getIO(), new SpeechParameters(
                        "A", super.getLocalStringVariableValue("strike")));
            }
        }
        return super.onStrike();
    }
    @Override
    public int onTargetDeath() throws RPGException {
        // IF ( §care_about_mice == 1 ) {
        // GOSUB MICE_DEATH
        // }
        if (Script.getInstance().getEventSender().getRefId() == ProjectConstants
                .getInstance().getPlayer()) {
            setLocalVarEnemy(false);
            setLocalVarFightingMode(0);
            if (getLocalVarTmpInt1() == super.getIO().getRefId()) {
                Script.getInstance().speak(super.getIO(),
                        new SpeechParameters("O A", getLocalSpeechVictory()));
                // PLAYANIM -e GRUNT
                goHome();
            } else {
                goHome();
            }
        } else if (getLocalVarSummonAttacking() == 1) {
            // this is for summoned monsters (undeads and demons)
            // - not defined here
            // this.summonSuite();
        }
        return super.onTargetDeath();
    }
    @Override
    public int onUndetectPlayer() throws RPGException {
        System.out.println("undetect");
        setLocalVarPlayerInSight(false);
        if (!getLocalVarControlsOff()) {
            if (super.getIO().isInGroup("UNDEAD")) {
                // TIMERmain -i 0 2 GOTO MAIN_ALERT
                ScriptTimerInitializationParameters timerParams =
                        new ScriptTimerInitializationParameters();
                timerParams.setName("main");
                timerParams.setScript(this);
                timerParams.setIo(super.getIO());
                timerParams.setFlagValues(1); // for -i flag
                timerParams.setRepeatTimes(0);
                timerParams.setMilliseconds(2000);
                timerParams.setStartTime(Time.getInstance().getGameTime());
                try {
                    timerParams.setObj(this);
                    timerParams.setMethod(getClass().getMethod("mainAlert"));
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                            e);
                }
                Script.getInstance().startTimer(timerParams);
                timerParams = null;
            }
            if (getLocalVarEnemy()) {
                // SET_NPC_STAT BACKSTAB 1 ACCEPT
            } else {
                if (super.getLocalIntVariableValue("panicmode") == 0) {
                    // didn't find player
                    // turn off the 'doubting' timer
                    Script.getInstance().timerClearByNameAndIO("doubting",
                            super.getIO());
                    super.setLocalVariable("panicmode", 1);
                    // TIMERabandon 1 5 GOTO GO_HOME
                    ScriptTimerInitializationParameters timerParams =
                            new ScriptTimerInitializationParameters();
                    timerParams.setName("abandon");
                    timerParams.setScript(this);
                    timerParams.setIo(super.getIO());
                    timerParams.setRepeatTimes(1);
                    timerParams.setMilliseconds(5000);
                    timerParams.setStartTime(Time.getInstance().getGameTime());
                    try {
                        timerParams.setObj(this);
                        timerParams.setMethod(getClass().getMethod("goHome"));
                    } catch (NoSuchMethodException | SecurityException e) {
                        throw new RPGException(
                                ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
                    }
                    Script.getInstance().startTimer(timerParams);
                    // TIMERquiet 1 6 SPEAK ~£back2guard~
                    timerParams =
                            new ScriptTimerInitializationParameters();
                    timerParams.setName("quiet");
                    timerParams.setScript(this);
                    timerParams.setIo(super.getIO());
                    timerParams.setRepeatTimes(1);
                    timerParams.setMilliseconds(6000);
                    timerParams.setStartTime(Time.getInstance().getGameTime());
                    try {
                        timerParams.setObj(Script.getInstance());
                        timerParams.setMethod(Script.class.getMethod("speak",
                                BaseInteractiveObject.class,
                                SpeechParameters.class));
                        timerParams.setArgs(new Object[] {
                                super.getIO(),
                                new SpeechParameters("",
                                        super.getLocalStringVariableValue(
                                                "back2guard"))
                        });
                    } catch (NoSuchMethodException | SecurityException e) {
                        throw new RPGException(
                                ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
                    }
                    Script.getInstance().startTimer(timerParams);
                    timerParams = null;
                }
                if (super.getLocalIntVariableValue("fighting_mode") == 1) {
                    // SET_NPC_STAT BACKSTAB 1
                    if (Script.getInstance().getGlobalTargetParam(
                            super.getIO()) == ProjectConstants.getInstance()
                                    .getPlayer()) {
                        if (super.getLocalIntVariableValue(
                                "looking_for") == 0) {
                            if (Script.getInstance()
                                    .isPlayerInvisible(super.getIO())) {
                                lookForSuite();
                            }
                            if (getLocalVarConfused()) {
                                lookForSuite();
                            }
                            super.setLocalVariable("looking_for", 1);
                            setLocalVarReflectionMode(RM_NOTHING);
                            // TIMERlookfor 1 3 GOTO LOOK_FOR
                            ScriptTimerInitializationParameters timerParams =
                                    new ScriptTimerInitializationParameters();
                            timerParams.setName("lookfor");
                            timerParams.setScript(this);
                            timerParams.setIo(super.getIO());
                            timerParams.setRepeatTimes(1);
                            timerParams.setMilliseconds(3000);
                            timerParams.setStartTime(
                                    Time.getInstance().getGameTime());
                            try {
                                timerParams.setObj(this);
                                timerParams.setMethod(
                                        Script.class.getMethod("lookFor"));
                            } catch (NoSuchMethodException
                                    | SecurityException e) {
                                throw new RPGException(
                                        ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
                            }
                            Script.getInstance().startTimer(timerParams);
                            timerParams = null;
                            Script.getInstance().sendEvent(super.getIO(),
                                    new SendParameters(
                                            null, // no init params
                                            null, // no group name
                                            new Object[] {
                                                    "speech_interval", 3,
                                                    "speech_parameters", "N",
                                                    "speech_text",
                                                    super.getLocalStringVariableValue(
                                                            "comeback")
                                            },
                                            "onSpeakNoRepeat", // event name
                                            "SELF", // target
                                            0 // radius not needed
                                    ));
                        }
                    }
                }
            }
        }
        return super.onUndetectPlayer();
    }
    /**
     * Starts the ouch event.
     * @throws PooledException if an error occurs
     * @throws RPGException if an error occurs
     */
    private void ouchStart() throws RPGException {
        // System.out.print("OUCH ");
        float ouchDmg = getLocalVarSummonedOuch() + getLocalVarOuch();
        // System.out.print(ouchDmg);
        // speak combat message first
        if (getLocalVarCombatMessage().length() > 0
                && ouchDmg > 0) {
            Script.getInstance().speak(super.getIO(),
                    new SpeechParameters("",
                            String.format(getLocalVarCombatMessage(),
                                    (int) ouchDmg)));
        }
        float painThreshold = super.getIO().getNPCData().getMaxLife();
        // System.out.print(" PAIN THRESHOLD ");
        // System.out.println(painThreshold);
        if (ouchDmg <= 1) {
            if (Script.getInstance()
                    .getGlobalIntVariableValue("PLAYERCASTING") == 0) {
                // Script.getInstance().forceAnimation(HIT_SHORT);
            }
            if (!getLocalVarEnemy()) {
                // clear all speech
                Script.getInstance().speak(super.getIO(),
                        new SpeechParameters("", ""));
            }
        } else { // damage is above pain threshold
            long tmp = Script.getInstance().getGlobalIntVariableValue(
                    "COMBATROUND");
            tmp -= getLocalVarOuchTime();
            if (tmp > 3) {
                // been more than 3 rounds since last recorded ouch?
                // force hit animation
                // Script.getInstance().forceAnimation(HIT);
                // set current time as last ouch
                setLocalVarOuchTime(
                        Script.getInstance().getGlobalIntVariableValue(
                                "COMBATROUND"));
            }
            if (ouchDmg >= painThreshold * 0.5) {
                if (Diceroller.getInstance().rolldX(2) == 2) {
                    // speak angrily "ouch_strong"
                    Script.getInstance().speak(super.getIO(),
                            new SpeechParameters("A",
                                    getLocalSpeechOuchStrong()));
                }
            } else {
                if (ouchDmg >= painThreshold * 0.25) {
                    if (Diceroller.getInstance().rolldX(2) == 2) {
                        // speak angrily "ouch_medium"
                        Script.getInstance().speak(super.getIO(),
                                new SpeechParameters("A",
                                        getLocalSpeechOuchMedium()));
                    }
                } else {
                    if (Diceroller.getInstance().rolldX(2) == 2) {
                        // speak angrily "ouch"
                        Script.getInstance().speak(super.getIO(),
                                new SpeechParameters("A",
                                        getLocalSpeechOuch()));
                    }
                }
            }
        }
    }
    private void ouchSuite() throws RPGException {
        if (getLocalVarControlsOff()) {
            // don't react to aggression
        } else {
            if (Script.getInstance().getEventSender().hasIOFlag(
                    IoGlobals.IO_01_PC)) {
                if (!getLocalVarPlayerInSight()) {
                    // player not in sight
                    setLocalVarEnemy(true);
                    // LOOK FOR ATTACKER
                }
                // turn off aggression - io is in combat
                super.assignDisallowedEvent(ScriptConsts.DISABLE_AGGRESSION);
                setLocalVarSpotted(true);
                attackPlayerAfterOuch();
            }
        }
    }
    public int playerDetected() throws RPGException {
        // if player is invisible ignore the event
        // IF (^PLAYERSPELL_INVISIBILITY == 1) ACCEPT

        // if IO is confused, ignore the event
        if (!getLocalVarConfused()) {
            setLocalVarPlayerInSight(true);
            if (!getLocalVarControlsOff()) {
                // SET_NPC_STAT BACKSTAB 0
                if (getLocalVarEnemy()
                        && super.getLocalIntVariableValue("fighting_mode") != 2
                        && !getLocalVarSleeping()) {
                    if (super.getLocalIntVariableValue("panicmode") > 0) {
                        attackPlayer();
                        // } ELSE IF (^DIST_PLAYER < 600) GOTO ATTACK_PLAYER
                    } else {
                        // set doubting timer
                        ScriptTimerInitializationParameters timerParams =
                                new ScriptTimerInitializationParameters();
                        timerParams.setName("doubting");
                        timerParams.setScript(this);
                        timerParams.setFlagValues(0);
                        timerParams.setIo(super.getIO());
                        timerParams.setMilliseconds(3000);
                        timerParams.setStartTime(
                                Time.getInstance().getGameTime());
                        timerParams.setRepeatTimes(1);
                        try {
                            timerParams.setObj(this);
                            timerParams.setMethod(
                                    getClass().getMethod("attackPlayer"));
                        } catch (NoSuchMethodException | SecurityException e) {
                            throw new RPGException(
                                    ErrorMessage.INTERNAL_BAD_ARGUMENT, e);
                        }
                        Script.getInstance().startTimer(timerParams);
                        timerParams = null;
                        // set panic mode to 2 to start doubting
                        super.setLocalVariable("panicmode", 2);
                        super.setLocalVariable("noise_heard", 2);
                        super.setLocalVariable("looking_for", 0);
                        // speak who goes there
                        // SPEAK -a ~£whogoesthere~ NOP
                        Script.getInstance().speak(super.getIO(),
                                new SpeechParameters("",
                                        super.getLocalStringVariableValue(
                                                "whogoesthere")));
                        setLocalVarReflectionMode(RM_NOTHING);
                        // kill timer quiet
                        Script.getInstance().timerClearByNameAndIO("quiet",
                                super.getIO());
                        saveBehavior();
                        super.getIO().getNPCData()
                                .addBehavior(Behaviour.BEHAVIOUR_MOVE_TO);
                        // SETTARGET PLAYER
                        super.getIO().getNPCData()
                                .setMovemode(IoGlobals.WALKMODE);
                    }
                }
            }
        }
        return ScriptConsts.ACCEPT;
    }
    private void reachedSound() throws RPGException {
        if (super.getIO().getTargetinfo() == Interactive.getInstance()
                .getTargetByNameTarget(getLocalVarLastHeard())) {
            super.behavior(new BehaviorParameters("FRIENDLY", 0));
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("OBJECT_");
                sb.append(getLocalVarLastHeard());
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            super.setTarget(new TargetParameters(sb.toString()));
            sb.returnToPool();
            sb = null;
            setLocalVarLastHeard("NOHEAR");
            if (getLocalVarLookingFor() != 0) {
                lookForSuite();
            } else {
                // TIMERheard 1 6 SENDEVENT SPEAK_NO_REPEAT SELF "3 N
                // £back2guard"
                ScriptTimerInitializationParameters timerParams =
                        new ScriptTimerInitializationParameters();
                timerParams.setName("heard");
                timerParams.setScript(this);
                timerParams.setIo(super.getIO());
                timerParams.setRepeatTimes(1);
                timerParams.setMilliseconds(6000);
                timerParams.setStartTime(Time.getInstance().getGameTime());
                try {
                    timerParams.setObj(Script.getInstance());
                    timerParams.setMethod(Script.class.getMethod("sendEvent",
                            BaseInteractiveObject.class, SendParameters.class));
                    timerParams.setArgs(new Object[] {
                            super.getIO(),
                            new SendParameters(null, null, new Object[] {
                                    "speech_interval", 3,
                                    "speech_parameters", "N",
                                    "speech_text", "back2guard"
                            }, "onSpeakNoRepeat", "SELF", 0)
                    });
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                            e);
                }
                Script.getInstance().startTimer(timerParams);
                timerParams = null;
            }
        } else if (super.getIO()
                .getTargetinfo() == super.getLocalIntVariableValue(
                        "helping_target")) {
            if (getLocalVarFightingMode() == FM_FLEE) {
                // SENDEVENT -rn DELATION 500 ~£flee_marker~
                Script.getInstance().sendEvent(super.getIO(),
                        new SendParameters("RADIUS NPC", null, new Object[] {
                                "tmp_string", getLocalVarFleeMarker()
                        }, "onDelation", "SELF", 500));
                setLocalVarCowardice(getLocalVarCowardice() / 2);
                setLocalVarHelpingBuddy("NOBUDDY");
                // newto test
                setLocalVarReflectionMode(0);
                setLocalVarHelpingTarget(getLocalVarFleeMarker());
                super.behavior(new BehaviorParameters("MOVE_TO", 0));
                PooledStringBuilder sb =
                        StringBuilderPool.getInstance().getStringBuilder();
                try {
                    sb.append("-a ");
                    sb.append(getLocalVarHelpingTarget());
                } catch (PooledException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
                }
                super.setTarget(new TargetParameters(sb.toString()));
                sb.returnToPool();
                sb = null;
                super.getIO().getNPCData().setMovemode(IoGlobals.RUNMODE);
            }
        }
    }
    public void resetPathFail() throws RPGException {
        setLocalVarIgnoreFailure(false);
        goHome();
    }
    private void restoreBehavior() throws RPGException {
        // check to see if a behavior was stacked
        if (super.getLocalIntVariableValue("main_behavior_stacked") == 1) {
            // CLEAR_MICE_TARGET
            System.out.println("unstack");
            // BEHAVIOR UNSTACK
            super.behavior(new BehaviorParameters("UNSTACK", 0));
            super.setLocalVariable("main_behavior_stacked", 0);
        } else if (!super.getLocalStringVariableValue(
                "init_marker").equalsIgnoreCase("NONE")) {
            // send NPC back to init marker
            // BEHAVIOR MOVE_TO
            super.behavior(new BehaviorParameters("MOVE_TO", 0));
            // SETTARGET -a ~£init_marker~
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            try {
                sb.append("-a OBJECT_");
                sb.append(super.getLocalStringVariableValue("init_marker"));
            } catch (PooledException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            super.setTarget(new TargetParameters(sb.toString()));
            sb.returnToPool();
            sb = null;
            super.getIO().getNPCData().setMovemode(IoGlobals.WALKMODE);
        } else {
            System.out.println("go home");
            super.behavior(new BehaviorParameters("GO_HOME", 0));
            // SETTARGET PLAYER
            super.setTarget(new TargetParameters("PLAYER"));
        }
    }
    /**
     * Saves the current behavior in the behavior stack. After processing, the
     * current behavior is in the stack and variable 'main_behavior_stacked' is
     * true. If the NPC had the variable 'frozen' set to true, then the stacked
     * behavior is 'FRIENDLY', and the NPC's target is the player.
     * @throws RPGException if an error occurs
     */
    private void saveBehavior() throws RPGException {
        // turn off collided with player timer
        Script.getInstance().timerClearByNameAndIO("colplayer", super.getIO());
        if (super.getLocalIntVariableValue("main_behavior_stacked") == 0) {
            if (super.getLocalIntVariableValue("frozen") == 1) {
                // frozen anim -> wake up !
                super.setLocalVariable("frozen", 0);
                // PLAYANIM NONE
                // PLAYANIM -2 NONE
                // PHYSICAL ON
                // turn on collisions
                super.removeDisallowedEvent(ScriptConsts.DISABLE_COLLIDE_NPC);
                // BEHAVIOR FRIENDLY
                super.behavior(new BehaviorParameters("FRIENDLY", 0));
                // SETTARGET PLAYER
                super.setTarget(new TargetParameters("PLAYER"));
            }
            super.setLocalVariable("main_behavior_stacked", 1);
            super.behavior(new BehaviorParameters("STACK", 0));
        } else {
            // behavior already saved : clear mice target if one
            // CLEAR_MICE_TARGET
        }
    }
    private void setLocalSpeechAggression(final String text)
            throws RPGException {
        super.setLocalVariable("sp_aggression", text);
    }
    private void setLocalSpeechDying(final String text) throws RPGException {
        super.setLocalVariable("sp_dying", text);
    }
    private void setLocalSpeechMisc(final String text) throws RPGException {
        super.setLocalVariable("sp_misc", text);
    }
    private void setLocalSpeechOuch(final String text) throws RPGException {
        super.setLocalVariable("sp_ouch", text);
    }
    private void setLocalSpeechOuchMedium(final String text)
            throws RPGException {
        super.setLocalVariable("sp_ouch_medium", text);
    }
    private void setLocalSpeechOuchStrong(final String text)
            throws RPGException {
        super.setLocalVariable("sp_ouch_strong", text);
    }
    private void setLocalSpeechSearch(final String text) throws RPGException {
        super.setLocalVariable("sp_search", text);
    }
    private void setLocalSpeechShortReflection(final String[] text)
            throws RPGException {
        super.setLocalVariable("sp_short_misc_reflections", text);
    }
    private void setLocalSpeechThief(final String text) throws RPGException {
        super.setLocalVariable("sp_thief", text);
    }
    private void setLocalSpeechThreat(final String text) throws RPGException {
        super.setLocalVariable("sp_threat", text);
    }
    private void setLocalSpeechVictory(final String text) throws RPGException {
        super.setLocalVariable("sp_victory", text);
    }
    private void setLocalVarBackToGuard(final String val) throws RPGException {
        super.setLocalVariable("back2guard", val);
    }
    private void setLocalVarCombatMessage(final String val)
            throws RPGException {
        super.setLocalVariable("combat_message", val);
    }
    private void setLocalVarConfused(final boolean val)
            throws RPGException {
        if (val) {
            super.setLocalVariable("confused", 1);
        } else {
            super.setLocalVariable("confused", 0);
        }
    }
    private void setLocalVarControlsOff(final boolean val)
            throws RPGException {
        if (val) {
            super.setLocalVariable("controls_off", 1);
        } else {
            super.setLocalVariable("controls_off", 0);
        }
    }
    private void setLocalVarCowardice(final int val) throws RPGException {
        super.setLocalVariable("cowardice", val);
    }
    /**
     * Sets the flag indicating whether the player is an enemy.
     * @param val the flag
     * @throws RPGException if an error occurs
     */
    private void setLocalVarEnemy(final boolean val) throws RPGException {
        if (!val) {
            super.setLocalVariable("enemy", 0);
        } else {
            super.setLocalVariable("enemy", 1);
        }
    }
    private void setLocalVarFightingMode(final int val) throws RPGException {
        super.setLocalVariable("fighting_mode", val);
    }
    private void setLocalVarFleeMarker(final String val) throws RPGException {
        super.setLocalVariable("flee_marker", val);
    }
    private void setLocalVarHelpingBuddy(final String val) throws RPGException {
        super.setLocalVariable("helping_buddy", val);
    }
    private void setLocalVarHelpingTarget(final String val)
            throws RPGException {
        super.setLocalVariable("helping_target", val);
    }
    private void setLocalVarIgnoreFailure(final boolean val)
            throws RPGException {
        if (!val) {
            super.setLocalVariable("ignorefailure", 0);
        } else {
            super.setLocalVariable("ignorefailure", 1);
        }
    }
    private void setLocalVarJustYouWait(final String val) throws RPGException {
        super.setLocalVariable("justyouwait", val);
    }
    private void setLocalVarLastCallForHelp(final int val) throws RPGException {
        super.setLocalVariable("last_call_help", val);
    }
    private void setLocalVarLastHeard(final String val) throws RPGException {
        super.setLocalVariable("last_heard", val);
    }
    private void setLocalVarLastReflection(final long val) throws RPGException {
        super.setLocalVariable("last_reflection", val);
    }
    private void setLocalVarLookingFor(final int val) throws RPGException {
        super.setLocalVariable("looking_for", val);
    }
    private void setLocalVarNoiseHeard(final int val) throws RPGException {
        super.setLocalVariable("noise_heard", val);
    }
    private void setLocalVarOuch(final float val) throws RPGException {
        super.setLocalVariable("OUCH", val);
    }
    private void setLocalVarOuchTime(final int val) throws RPGException {
        super.setLocalVariable("ouch_time", val);
    }
    private void setLocalVarPain(final int val) throws RPGException {
        super.setLocalVariable("pain", val);
    }
    private void setLocalVarPanicMode(final int val) throws RPGException {
        super.setLocalVariable("panicmode", val);
    }
    private void setLocalVarPlayerEnemySend(final boolean flag)
            throws RPGException {
        if (flag) {
            super.setLocalVariable("player_enemy_send", 1);
        } else {
            super.setLocalVariable("player_enemy_send", 0);
        }
    }
    private void setLocalVarPlayerInSight(final boolean flag)
            throws RPGException {
        if (flag) {
            super.setLocalVariable("player_in_sight", 1);
        } else {
            super.setLocalVariable("player_in_sight", 0);
        }
    }
    /**
     * Sets the script's reflection mode: NOTHING, NORMAL, THREAT, or SEARCH.
     * @param val the new mode
     * @throws RPGException if an error occurs
     */
    private void setLocalVarReflectionMode(final int val) throws RPGException {
        super.setLocalVariable("reflection_mode", val);
    }
    /**
     * Sets the flag indicating the script's IO is sleeping.
     * @param val
     * @throws RPGException
     */
    private void setLocalVarSleeping(final boolean flag) throws RPGException {
        if (flag) {
            super.setLocalVariable("sleeping", 1);
        } else {
            super.setLocalVariable("sleeping", 0);
        }
    }
    private void setLocalVarSpotted(final boolean flag)
            throws RPGException {
        if (flag) {
            super.setLocalVariable("spotted", 1);
        } else {
            super.setLocalVariable("spotted", 0);
        }
    }
    private void setLocalVarSummonAttacking(final int val) throws RPGException {
        super.setLocalVariable("summon_attacking", val);
    }
    private void setLocalVarSummonedOuch(final float val) throws RPGException {
        super.setLocalVariable("SUMMONED_OUCH", val);
    }
    private void setLocalVarTmpInt1(final int val) throws RPGException {
        super.setLocalVariable("tmp_int1", val);
    }
    private void setLocalVarTmpString1(final String val) throws RPGException {
        super.setLocalVariable("tmp_string1", val);
    }
    private void setLocalVarTmpString2(final String val) throws RPGException {
        super.setLocalVariable("tmp_string2", val);
    }
    private void setLocalVarTotalDead(final boolean flag)
            throws RPGException {
        if (flag) {
            super.setLocalVariable("totaldead", 1);
        } else {
            super.setLocalVariable("totaldead", 0);
        }
    }
    private void setLocalVarType(final String val) throws RPGException {
        super.setLocalVariable("type", val);
    }
    private void setLocalVarVictory(final String val) throws RPGException {
        super.setLocalVariable("victory", val);
    }
    private void tryingToReachPlayer() throws RPGException {
        if (super.getLocalStringVariableValue("fake_target")
                .equalsIgnoreCase("FAKE")) {
            int lastHeardIoId = Interactive.getInstance().getTargetByNameTarget(
                    getLocalVarLastHeard());
            if (lastHeardIoId == ProjectConstants.getInstance().getPlayer()) {
                reachedSound();
            }
        } else {
            if (getLocalVarFightingMode() != FM_FLEE
                    && !getLocalVarControlsOff()) {
                if (!Script.getInstance().isPlayerInvisible(super.getIO())) {
                    playerDetected();
                } else if (getLocalVarLookingFor() != 0) {
                    lookForSuite();
                } else {
                    if (getLocalVarFightingMode() == 1) {
                        if (!getLocalVarConfused()
                                && !Script.getInstance()
                                        .isPlayerInvisible(super.getIO())
                                && getLocalVarReflectionMode() != OrcSentry.RM_THREAT) {
                            setLocalVarReflectionMode(RM_THREAT);
                        }
                    }
                }
            }
        }
    }
}
