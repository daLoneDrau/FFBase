package com.dalonedrow.module.ff.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class FFEquipmentElementTest {
    @Before
    public void before() {

    }
    /** Gets the number of element values. */
    @Test
    public void canGetNumberOfValues() {
        final int num = 7;
        assertEquals("should have " + num + " elements", num,
                FFEquipmentElements.getNumberOfValues());
    }
}
