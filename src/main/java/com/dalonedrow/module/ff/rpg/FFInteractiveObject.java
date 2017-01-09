package com.dalonedrow.module.ff.rpg;

import com.dalonedrow.rpg.base.flyweights.BaseInteractiveObject;
import com.dalonedrow.rpg.base.flyweights.RPGException;

/**
 * @author drau
 */
public class FFInteractiveObject extends BaseInteractiveObject<FFItem,
        FFInventory, FFCharacter, FFNpc, FFScriptable> {
    /** door data. */
    private FFDoorData doorData;
    /** room data. */
    private FFRoomData roomData;
    /**
     * Creates a new instance of {@link FFInteractiveObject}.
     * @param id the IO id
     */
    public FFInteractiveObject(final int id) {
        super(id);
        super.setInventory(new FFInventory());
        super.getInventory().setIo(this);
        super.setItemData(new FFItem());
    }
    /**
     * Gets the door data.
     * @return {@link FFDoorData}
     */
    public final FFDoorData getDoorData() {
        return doorData;
    }
    /**
     * Gets the room data.
     * @return {@link FFRoomData}
     */
    public final FFRoomData getRoomData() {
        return roomData;
    }
    /**
     * Sets the door data.
     * @param data the {@link FFDoorData} data
     */
    public final void setDoorData(final FFDoorData data) {
        doorData = data;
    }
    /**
     * Sets the room data.
     * @param data the {@link FFRoomData} data
     */
    public final void setRoomData(final FFRoomData data) {
        roomData = data;
    }
}
