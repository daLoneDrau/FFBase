package com.dalonedrow.module.ff.systems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.Script;

public class CombatTest {
    private Combat combat;
    @Before
    public void before() throws IOException, RPGException {
        new FFController();
        new FFInteractive();
        new FFScript();
        new FFSpeech();
        new FFSpellController();
        combat = new Combat();
        FFWorldMap.getInstance().load();
    }
    @Test
    public void canKillPlayer() throws RPGException {
        FFInteractiveObject playerIO =
                ((FFInteractive) Interactive.getInstance()).newHero();
        playerIO.setPosition(new SimpleVector2());
        assertTrue("Player was created", Interactive.getInstance().hasIO(
                ProjectConstants.getInstance().getPlayer()));
        FFInteractiveObject enemyIO =
                FFWebServiceClient.getInstance().loadNPC("ORC_SENTRY");
        // WHEN IO ADDED TO SCENE, SHOW SET TO 1, POSITION SET
        enemyIO.setShow(1);
        enemyIO.setPosition(new SimpleVector2());
        enemyIO.setInitPosition(new SimpleVector2());
        assertEquals("enemy stamina is 5", 5,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        combat.addEnemy(enemyIO);
        while (!combat.isOver()) {
            combat.setTestingMode(Combat.TEST_MODE_CREATURE_HITS);
            combat.doRound();
            for (int i = 0, len = combat.getMessages().length; i < len; i++) {
                System.out.println(combat.getMessages()[i]);
            }
            System.out.print("-------------ROUND ");
            System.out.println(Script.getInstance()
                    .getGlobalIntVariableValue("COMBATROUND"));
        }
        System.out.println("end killplayer\n\n\n");
    }
    @Test
    public void canKillSimpleEnemy() throws RPGException {
        ((FFInteractive) Interactive.getInstance()).newHero();
        ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                .setPosition(new SimpleVector2());
        assertTrue("Player was created", Interactive.getInstance().hasIO(
                ProjectConstants.getInstance().getPlayer()));
        FFInteractiveObject enemyIO =
                FFWebServiceClient.getInstance().loadNPC("ORC_SENTRY");
        // WHEN IO ADDED TO SCENE, SHOW SET TO 1, POSITION SET
        enemyIO.setShow(1);
        enemyIO.setPosition(new SimpleVector2());
        enemyIO.setInitPosition(new SimpleVector2());
        assertEquals("enemy stamina is 5", 5,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        combat.addEnemy(enemyIO);
        // test player hitting
        // *********************************************************************
        // ROUND 1
        combat.setTestingMode(Combat.TEST_MODE_PLAYER_HITS);
        combat.doRound();
        assertEquals("round 1",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                1);
        // aggression script should run - does nothing.
        // call damageNPC
        // send NPC ouch event - results in speech after 4 rounds
        // send hit event
        // process damage
        assertEquals("after round 1, enemy stamina is 3", 3,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        // no ouch time because pain threshold > damage
        assertEquals("enemy hit in round 0", 0,
                enemyIO.getScript().getLocalIntVariableValue("ouch_time"));

        // reset enemy health
        enemyIO.getNPCData().healNPC(2);
        assertEquals("enemy stamina is 5", 5,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertFalse(combat.isOver());
        System.out.println("-------------ROUND 1");
        // *********************************************************************
        // ROUND 2
        combat.setTestingMode(Combat.TEST_MODE_PLAYER_HITS_LUCKY);
        combat.doRound();
        assertEquals("round 2",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                2);
        assertEquals("after round 2, enemy stamina is 1", 1,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        assertEquals("last ouch still round 0", 0,
                enemyIO.getScript().getLocalIntVariableValue("ouch_time"));
        // reset enemy health
        enemyIO.getNPCData().healNPC(4);
        assertEquals("enemy stamina is 5", 5,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        assertNotNull("round 2 enemy script not null", enemyIO.getScript());
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertFalse(combat.isOver());
        System.out.println("-------------ROUND 2");
        // *********************************************************************
        // ROUND 3
        combat.setTestingMode(Combat.TEST_MODE_PLAYER_HITS_UNLUCKY);
        combat.doRound();
        assertEquals("round 3",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                3);
        assertEquals("after round 3, enemy stamina is 4", 4,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        assertEquals("last ouch still round 0", 0,
                enemyIO.getScript().getLocalIntVariableValue("ouch_time"));
        // reset enemy health
        enemyIO.getNPCData().healNPC(1);
        assertEquals("enemy stamina is 5", 5,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        assertNotNull("round 3 enemy script not null", enemyIO.getScript());
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertFalse(combat.isOver());
        System.out.println("-------------ROUND 3");
        // *********************************************************************
        // ROUND 4
        float plrST = ((FFController) ProjectConstants.getInstance())
                .getPlayerIO().getPCData()
                .getFullAttributeScore("ST");
        combat.setTestingMode(Combat.TEST_MODE_CREATURE_HITS);
        combat.doRound();
        assertEquals("round 4",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                4);
        assertEquals("after round 4, player takes 2 damage", plrST - 2,
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData()
                        .getFullAttributeScore("ST"),
                0.000001f);
        // reset player health
        ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                .getPCData()
                .ARX_DAMAGES_HealPlayer(5);
        assertEquals("player stamina is full", plrST,
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData()
                        .getFullAttributeScore("ST"),
                0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertFalse(combat.isOver());
        System.out.println("-------------ROUND 4");
        // *********************************************************************
        // ROUND 5
        combat.setTestingMode(Combat.TEST_MODE_CREATURE_HITS_LUCKY);
        combat.doRound();
        assertEquals("round 5",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                5);
        assertEquals("after round 5, player takes 1 damage", plrST - 1,
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData()
                        .getFullAttributeScore("ST"),
                0.000001f);
        // reset player health
        ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                .getPCData()
                .ARX_DAMAGES_HealPlayer(5);
        assertEquals("player stamina is full", plrST,
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData()
                        .getFullAttributeScore("ST"),
                0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertFalse(combat.isOver());
        System.out.println("-------------ROUND 5");
        // *********************************************************************
        // ROUND 6
        combat.setTestingMode(Combat.TEST_MODE_CREATURE_HITS_UNLUCKY);
        combat.doRound();
        assertEquals("round 6",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                6);
        assertEquals("after round 6, player takes 3 damage", plrST - 3,
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData()
                        .getFullAttributeScore("ST"),
                0.000001f);
        // reset player health
        ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                .getPCData()
                .ARX_DAMAGES_HealPlayer(5);
        assertEquals("player stamina is full", plrST,
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData()
                        .getFullAttributeScore("ST"),
                0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertFalse(combat.isOver());
        System.out.println("-------------ROUND 6");
        // *********************************************************************
        // ROUND 7
        combat.setTestingMode(Combat.TEST_MODE_BOTH_MISS);
        combat.doRound();
        assertEquals("round 7",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                7);
        assertEquals("after round 7, player takes no damage", plrST,
                ((FFController) ProjectConstants.getInstance()).getPlayerIO()
                        .getPCData()
                        .getFullAttributeScore("ST"),
                0.000001f);
        assertEquals("after round 7, enemy stamina is 5", 5,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertFalse(combat.isOver());
        System.out.println("-------------ROUND 7");
        // *********************************************************************
        // ROUND 8
        combat.setTestingMode(Combat.TEST_MODE_PLAYER_HITS_LUCKY);
        combat.doRound();
        assertEquals("round 8",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                8);
        assertEquals("after round 7, enemy stamina is 1", 1,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertFalse(combat.isOver());
        System.out.println("-------------ROUND 8");
        // *********************************************************************
        // ROUND 9
        combat.setTestingMode(Combat.TEST_MODE_PLAYER_HITS_LUCKY);
        combat.doRound();
        assertEquals("round 9",
                Script.getInstance().getGlobalIntVariableValue("COMBATROUND"),
                9);
        assertEquals("after round 9, enemy stamina is 0", 0,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        assertTrue(combat.isOver());
        System.out.println("-------------ROUND 9");
        System.out.println("end killsimpleEnemy\n\n\n");
    }
    @Test
    public void canRunRandomRound() throws RPGException {
        FFInteractiveObject playerIO =
                ((FFInteractive) Interactive.getInstance()).newHero();
        playerIO.setPosition(new SimpleVector2(636,1337));
        assertTrue("Player was created", Interactive.getInstance().hasIO(
                ProjectConstants.getInstance().getPlayer()));
        FFInteractiveObject enemyIO =
                FFWebServiceClient.getInstance().loadNPC("ORC_SENTRY");
        // WHEN IO ADDED TO SCENE, SHOW SET TO 1, POSITION SET
        enemyIO.setShow(1);
        enemyIO.setPosition(new SimpleVector2(636,1338));
        enemyIO.setInitPosition(new SimpleVector2());
        assertEquals("enemy stamina is 5", 5,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        combat.addEnemy(enemyIO);
        while (!combat.isOver()) {
            combat.doRound();
            for (int i = 0, len = combat.getMessages().length; i < len; i++) {
                System.out.println(combat.getMessages()[i]);
            }
            System.out.print("-------------ROUND ");
            System.out.println(Script.getInstance()
                    .getGlobalIntVariableValue("COMBATROUND"));
            if (combat.defeated()) {
                combat.setLastMessageDisplayed(true);
            }
        }
        System.out.println("end canRunRandomRound\n\n\n");
    }
}
