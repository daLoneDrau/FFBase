package com.dalonedrow.module.ff.graph;

import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.graph.PhysicalGraphNode;

/**
 * 
 * @author 588648
 *
 */
public class FFMapNode extends PhysicalGraphNode {
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        String s = null;
        try {
            sb.append(this.getClass().getName());
            sb.append("(name=");
            sb.append(new String(super.getName()));
            sb.append(",index=");
            sb.append(super.getIndex());
            sb.append(",location=");
            sb.append(super.getLocation());
            sb.append(",roomNumber=");
            sb.append(this.roomNumber);
            sb.append(")");
        } catch (PooledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        s = sb.toString();
        sb.returnToPool();
        sb = null;
        return s;
    }
    /** the room number associated with the {@link FFMapNode}. */
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
