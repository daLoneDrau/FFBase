package com.dalonedrow.module.ff.rpg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.module.ff.systems.FFInteractive;
import com.dalonedrow.module.ff.systems.FFScript;
import com.dalonedrow.rpg.base.constants.EquipmentGlobals;
import com.dalonedrow.rpg.base.flyweights.RPGException;

public class FFCharacterTest {
    @Before
    public void before() throws IOException, RPGException {
        new FFController();
        new FFInteractive();
        new FFScript();
    }
    @Test
    public void canGetNewHero() throws RPGException {
        FFCharacter pc = new FFCharacter();
        assertNotNull("Pc is not null", pc);
        pc.computeFullStats();
        assertEquals("stamina is 0", 0, pc.getFullAttributeScore("ST"), 0.00f);
        assertEquals("max st is 0", 0, pc.getFullAttributeScore("MST"), 0.00f);
        assertEquals("luck is 0", 0, pc.getFullAttributeScore("LK"), 0.00f);
        assertEquals("max lk is 0", 0, pc.getFullAttributeScore("MLK"), 0.00f);
        assertEquals("skill is 0", 0, pc.getFullAttributeScore("SK"), 0.00f);
        assertEquals("max sk is 0", 0, pc.getFullAttributeScore("MSK"), 0.00f);
        assertEquals("dmg is 0", 0, pc.getFullAttributeScore("DMG"), 0.00f);
        int wid = pc.getEquippedItem(EquipmentGlobals.EQUIP_SLOT_WEAPON);
        assertTrue("weapon slot IS empty", wid == -1);

        pc = ((FFInteractive) Interactive.getInstance()).newHero().getPCData();
        assertTrue("stam is at least 14", 14 <= pc.getFullAttributeScore("ST"));
        assertTrue("skill is at most 24", 24 >= pc.getFullAttributeScore("ST"));
        assertEquals("stam=maxst", pc.getFullAttributeScore("ST"),
                pc.getFullAttributeScore("MST"), 0.00f);
        assertTrue("skill is at least 7", 7 <= pc.getFullAttributeScore("SK"));
        assertTrue("skill is at most 12", 12 >= pc.getFullAttributeScore("SK"));
        assertEquals("skill=maxsk", pc.getFullAttributeScore("SK"),
                pc.getFullAttributeScore("MSK"), 0.00f);
        assertTrue("luck is at least 7", 7 <= pc.getFullAttributeScore("LK"));
        assertTrue("luck is at most 12", 12 >= pc.getFullAttributeScore("LK"));
        assertEquals("luck=maxLk", pc.getFullAttributeScore("LK"),
                pc.getFullAttributeScore("MLK"), 0.00f);
        assertEquals("damage is 2", 2, pc.getFullAttributeScore("DMG"), 0.00f);
        wid = pc.getEquippedItem(EquipmentGlobals.EQUIP_SLOT_WEAPON);
        assertTrue("weapon slot NOT empty", wid >= 0);
        FFInteractiveObject wio =
                (FFInteractiveObject) Interactive.getInstance().getIO(wid);
        assertEquals("weapon is iron sword", "Iron Sword",
                new String(wio.getItemData().getItemName()));
    }
}
