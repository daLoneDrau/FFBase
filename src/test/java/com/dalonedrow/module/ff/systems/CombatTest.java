package com.dalonedrow.module.ff.systems;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.rpg.base.flyweights.RPGException;

public class CombatTest {
    private Combat combat;
    @Before
    public void before() throws IOException, RPGException {
        new FFController();
        new FFInteractive();
        new FFScript();
        new FFSpeech();
        combat = new Combat();
    }
    @Test
    public void canSetEnemy() throws RPGException {
        ((FFInteractive) FFInteractive.getInstance()).newHero();
        ((FFController) ProjectConstants.getInstance()).getPlayerIO()
        .setPosition(new SimpleVector2());
        assertTrue("Player was created", Interactive.getInstance().hasIO(
                ProjectConstants.getInstance().getPlayer()));
        FFInteractiveObject enemyIO =
                FFWebServiceClient.getInstance().loadNPC("ORC_SENTRY");
        // WHEN IO ADDED TO SCENE, SHOW SET TO 1, POSITION SET
        enemyIO.setShow(1);
        enemyIO.setPosition(new SimpleVector2());
        assertEquals("enemy stamina is 5", 5,
                enemyIO.getNPCData().getFullAttributeScore("ST"), 0.000001f);
        assertNotNull("round 0 enemy script not null", enemyIO.getScript());
        combat.addEnemy(enemyIO);
        // test player hitting
        //*********************************************************************
        // ROUND 1
        combat.setTestingMode(Combat.TEST_MODE_PLAYER_HITS);
        combat.doRound();
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
        System.out.println("-------------ROUND 1");
        //*********************************************************************
        // ROUND 2
        combat.setTestingMode(Combat.TEST_MODE_PLAYER_HITS_LUCKY);
        combat.doRound();
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
        System.out.println("-------------ROUND 2");
        //*********************************************************************
        // ROUND 3
        combat.setTestingMode(Combat.TEST_MODE_PLAYER_HITS_UNLUCKY);
        combat.doRound();
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
        System.out.println("-------------ROUND 3");
        //*********************************************************************
        // ROUND 4
        float plrST = ((FFController)
                ProjectConstants.getInstance()).getPlayerIO().getPCData()
                .getFullAttributeScore("ST");
        combat.setTestingMode(Combat.TEST_MODE_CREATURE_HITS);
        combat.doRound();
        assertEquals("after round 4, player takes 2 damage", plrST - 2,
                ((FFController)
                        ProjectConstants.getInstance()).getPlayerIO().getPCData()
                        .getFullAttributeScore("ST"), 0.000001f);
        // reset player health
        ((FFController)
                ProjectConstants.getInstance()).getPlayerIO().getPCData()
        .ARX_DAMAGES_HealPlayer(5);
        assertEquals("player stamina is full", plrST,
                ((FFController)
                ProjectConstants.getInstance()).getPlayerIO().getPCData()
                .getFullAttributeScore("ST"), 0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        System.out.println("-------------ROUND 4");
        //*********************************************************************
        // ROUND 5
        combat.setTestingMode(Combat.TEST_MODE_CREATURE_HITS_LUCKY);
        combat.doRound();
        assertEquals("after round 5, player takes 1 damage", plrST - 1,
                ((FFController)
                        ProjectConstants.getInstance()).getPlayerIO().getPCData()
                        .getFullAttributeScore("ST"), 0.000001f);
        // reset player health
        ((FFController)
                ProjectConstants.getInstance()).getPlayerIO().getPCData()
        .ARX_DAMAGES_HealPlayer(5);
        assertEquals("player stamina is full", plrST,
                ((FFController)
                ProjectConstants.getInstance()).getPlayerIO().getPCData()
                .getFullAttributeScore("ST"), 0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        System.out.println("-------------ROUND 5");
        //*********************************************************************
        // ROUND 6
        combat.setTestingMode(Combat.TEST_MODE_CREATURE_HITS_UNLUCKY);
        combat.doRound();
        assertEquals("after round 6, player takes 3 damage", plrST - 3,
                ((FFController)
                        ProjectConstants.getInstance()).getPlayerIO().getPCData()
                        .getFullAttributeScore("ST"), 0.000001f);
        // reset player health
        ((FFController)
                ProjectConstants.getInstance()).getPlayerIO().getPCData()
        .ARX_DAMAGES_HealPlayer(5);
        assertEquals("player stamina is full", plrST,
                ((FFController)
                ProjectConstants.getInstance()).getPlayerIO().getPCData()
                .getFullAttributeScore("ST"), 0.000001f);
        for (int i = 0, len = combat.getMessages().length; i < len; i++) {
            System.out.println(combat.getMessages()[i]);
        }
        System.out.println("-------------ROUND 6");
    }
}
