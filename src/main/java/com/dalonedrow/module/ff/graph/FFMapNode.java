package com.dalonedrow.module.ff.graph;

import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.graph.PhysicalGraphNode;

public class FFMapNode extends PhysicalGraphNode {
    private int roomNumber;
    /**
     * Creates a new instance of {@link FFMapNode}.
     * @param name
     * @param ind
     * @param x
     * @param y
     * @throws RPGException
     */
    public FFMapNode(String name, int ind, int x, int y) throws RPGException {
        super(name, ind, x, y);
        // TODO Auto-generated constructor stub
    }
    /**
     * @return the roomNumber
     */
    public int getRoomNumber() {
        return roomNumber;
    }
    /**
     * @param roomNumber the roomNumber to set
     */
    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
}
