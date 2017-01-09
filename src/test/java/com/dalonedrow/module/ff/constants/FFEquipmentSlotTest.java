package com.dalonedrow.module.ff.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class FFEquipmentSlotTest {
    @Before
    public void before() {

    }
    /** Gets the number of element values. */
    @Test
    public void canGetNumberOfValues() {
        final int num = 2;
        assertEquals("should have " + num + " elements", num,
                FFEquipmentSlots.getNumberOfValues());
    }
}
