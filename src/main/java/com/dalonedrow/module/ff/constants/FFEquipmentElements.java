package com.dalonedrow.module.ff.constants;

import com.dalonedrow.engine.systems.base.JOGLErrorHandler;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.utils.ArrayUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FFEquipmentElements {
    /** the list of values. */
    private static FFEquipmentElements[] values = new FFEquipmentElements[0];
    static {
        try {
            load();
        } catch (RPGException e) {
            JOGLErrorHandler.getInstance().fatalError(e);
        }
    }
    /**
     * Gets the number of values.
     * @return {@link int}
     */
    public static int getNumberOfValues() {
        return values.length;
    }
    /**
     * Loads all values.
     * @throws RPGException
     */
    private static void load() throws RPGException {
        JsonArray results =
                FFWebServiceClient.getInstance().getEquipmentElements();
        for (int i = results.size() - 1; i >= 0; i--) {
            JsonObject obj = results.get(i).getAsJsonObject();
            FFEquipmentElements element;
            if (obj.has("index")) {
                element = new FFEquipmentElements(obj.get("code").getAsString(),
                        obj.get("value").getAsInt());
            } else {
                element = new FFEquipmentElements(obj.get("code").getAsString());
            }
            values = ArrayUtilities.getInstance().extendArray(element, values);
        }
    }
    /**
     * Gets the {@link FFEquipmentElementTest} value of a specific element code.
     * @param code the code
     * @return {@link FFEquipmentElementTest}
     */
    public static FFEquipmentElements valueOf(final String code) {
        FFEquipmentElements value = null;
        for (int i = values.length - 1; i >= 0; i--) {
            if (code.equalsIgnoreCase(new String(values[i].code))) {
                value = values[i];
                break;
            }
        }
        return value;
    }
    /** the code. */
    private final char[] code;
    /** the element index. */
    private final int index;
    /**
     * Hidden constructor.
     * @param c the code
     */
    private FFEquipmentElements(final String c) {
        this(c, 0);
    }
    /**
     * Hidden constructor.
     * @param c the code
     * @param i the index
     */
    private FFEquipmentElements(final String c, final int i) {
        code = c.toCharArray();
        index = i;
    }
    /**
     * Gets the element's index.
     * @return {@link int}
     */
    public int getIndex() {
        return index;
    }
}
