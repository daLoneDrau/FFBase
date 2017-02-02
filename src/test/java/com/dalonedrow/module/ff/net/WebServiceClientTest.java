package com.dalonedrow.module.ff.net;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;

import com.dalonedrow.module.ff.constants.FFEquipmentElements;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFItem;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.module.ff.systems.FFInteractive;
import com.dalonedrow.module.ff.systems.FFScript;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.rpg.base.flyweights.EquipmentItemModifier;
import com.dalonedrow.rpg.base.flyweights.RPGException;

public class WebServiceClientTest {
    @Before
    public void before() throws IOException {
        new FFController();
        new FFWebServiceClient();
        new FFInteractive();
        try {
            new FFScript();
        } catch (RPGException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void canGetInstance() {
        assertNotNull(FFWebServiceClient.getInstance());
    }
    @Test
    public void canGetAttributes() throws RPGException {
        FFWebServiceClient.getInstance().getAttributes();
    }
    @Test
    public void canGetModifier() throws RPGException {
        EquipmentItemModifier mod = 
                FFWebServiceClient.getInstance().getModifierByCode("PLUS_2");
        assertEquals("value is 2", 2, mod.getValue(), 0.00001f);
        assertFalse("not percent", mod.isPercentage());
    } 
    @Test
    public void canGetItem() throws RPGException {
        FFInteractiveObject io = FFWebServiceClient.getInstance().loadItem("Iron Sword");
        FFItem ironSword = io.getItemData();
        assertEquals("damage is 2", ironSword.getEquipitem().getElement(
                FFEquipmentElements.valueOf(
                        "ELEMENT_DAMAGE").getIndex()).getValue(), 2, 0.001f);
    }    
}
