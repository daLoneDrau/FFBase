package com.dalonedrow.module.ff.rpg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.module.ff.systems.FFInteractive;
import com.dalonedrow.module.ff.systems.FFScript;
import com.dalonedrow.rpg.base.constants.EquipmentGlobals;
import com.dalonedrow.rpg.base.flyweights.RPGException;

public class FFNPCTest {
    @Before
    public void before() throws IOException, RPGException {
        new FFController();
        new FFInteractive();
        new FFScript();
    }
    @Test
    public void canGetOrcSentry() throws RPGException {
        FFInteractiveObject io =
                FFWebServiceClient.getInstance().loadNPC("ORC_SENTRY");
        FFNpc npc = io.getNPCData();
        assertNotNull("NPC is not null", npc);
        npc.computeFullStats();
        assertEquals("stam is 5", 5, npc.getFullAttributeScore("ST"),
                0.000001f);
        assertEquals("stam is 5", 5, npc.getFullAttributeScore("MST"),
                0.00001f);
        assertEquals("skil is 6", 6, npc.getFullAttributeScore("SK"),
                0.000001f);
        assertEquals("skil is 6", 6, npc.getFullAttributeScore("MSK"),
                0.00001f);
        assertEquals("damage is 2", 2, npc.getFullDamage(), 0.00f);
        int wid = npc.getEquippedItem(EquipmentGlobals.EQUIP_SLOT_WEAPON);
        assertTrue("weapon slot NOT empty", wid >= 0);
        FFInteractiveObject wio =
                (FFInteractiveObject) Interactive.getInstance().getIO(wid);
        assertEquals("weapon is orc cleaver", "Orc Cleaver",
                new String(wio.getItemData().getItemName()));
    }
}
