package com.dalonedrow.module.ff.rpg;

import com.dalonedrow.engine.sprite.base.SimplePoint;
import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.rpg.base.constants.IoGlobals;
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
     * {@inheritDoc}
     * @throws RPGException
     */
    @Override
    public void setPosition(final SimplePoint val) throws RPGException {
        if (super.hasIOFlag(IoGlobals.IO_01_PC)) {
            // this is the player
            try {
                FFWorldMap.getInstance().getRoomByCellCoordinates(val)
                        .setVisited(true);
            } catch (RPGException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        super.setPosition(val);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(final SimpleVector2 val) {
        if (super.hasIOFlag(IoGlobals.IO_01_PC)) {
            // this is the player
            try {
                FFWorldMap.getInstance().getRoomByCellCoordinates(val)
                        .setVisited(true);
            } catch (RPGException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        super.setPosition(val);
    }
    /**
     * Sets the room data.
     * @param data the {@link FFRoomData} data
     */
    public final void setRoomData(final FFRoomData data) {
        roomData = data;
    }
}
