package com.dalonedrow.module.ff.rpg;

import com.dalonedrow.rpg.graph.GraphNode;
import com.dalonedrow.rpg.graph.PhysicalGraphNode;
import com.dalonedrow.utils.ArrayUtilities;

public class FFRoomNode {
    /** the room's id. */
    private final int id;
    /** the index of the room's main node. */
    private int mainNode;
    /** the list of {@link GraphNode}s the room contains. */
    private PhysicalGraphNode[] nodes;
    /**
     * Creates a new instance of {@link FFRoomNode}.
     * @param roomId the room's id
     */
    public FFRoomNode(int roomId) {
        id = roomId;
        mainNode = -1;
        nodes = new PhysicalGraphNode[0];
    }
    /**
     * Adds a {@link GraphNode} to the room.
     * @param node the {@link GraphNode}
     * @param isMain the flag indicating whether the node is the main node
     */
    public void addNode(GraphNode node, boolean isMain) {
        if (!hasNode(node.getIndex())) {
            nodes = (PhysicalGraphNode[]) ArrayUtilities.getInstance()
                    .extendArray(node, nodes);
        }
        if (isMain) {
            mainNode = node.getIndex();
        }
    }
    /**
     * Determines if a room contains a specific {@link GraphNode}.
     * @param node the {@link GraphNode}
     * @return <tt>true</tt> if the room has the node; <tt>false</tt> otherwise
     */
    public boolean contains(GraphNode node) {
        boolean contains = false;
        for (int i = nodes.length - 1; i >= 0; i--) {
            if (nodes[i].getIndex() == node.getIndex()) {
                contains = true;
                break;
            }
        }
        return contains;
    }
    /**
     * Gets the room's id.
     * @return {@link int}
     */
    public int getId() {
        return id;
    }
    /**
     * Gets the main node
     * @return {@link PhysicalGraphNode}
     */
    public PhysicalGraphNode getMainNode() {
        PhysicalGraphNode node = null;
        for (int i = nodes.length - 1; i >= 0; i--) {
            if (nodes[i].getIndex() == mainNode) {
                node = nodes[i];
                break;
            }
        }
        return node;
    }
    /**
     * Gets a node by its location.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return {@link PhysicalGraphNode}
     */
    public PhysicalGraphNode getNode(final int x, final int y) {
        PhysicalGraphNode node = null;
        for (int i = nodes.length - 1; i >= 0; i--) {
            if (nodes[i].equals(x, y)) {
                node = nodes[i];
                break;
            }
        }
        return node;
    }
    /**
     * Determines if a room contains a specific {@link GraphNode}.
     * @param node the {@link GraphNode}'s index
     * @return <tt>true</tt> if the room has the node; <tt>false</tt> otherwise
     */
    private boolean hasNode(int index) {
        boolean has = false;
        for (int i = nodes.length - 1; i >= 0; i--) {
            if (nodes[i].getIndex() == index) {
                has = true;
                break;
            }
        }
        return has;
    }
    /**
     * Determines if a room contains a specific {@link GraphNode}.
     * @param node the {@link GraphNode}'s index
     * @return <tt>true</tt> if the room has the node; <tt>false</tt> otherwise
     */
    public boolean hasNode(final int x, final int y) {
        boolean has = false;
        for (int i = nodes.length - 1; i >= 0; i--) {
            if (nodes[i].equals(x, y)) {
                has = true;
                break;
            }
        }
        return has;
    }
    /**
     * Sets the room's main node.
     * @param node the main node to set
     */
    public void setMainNode(final GraphNode node) {
        mainNode = node.getIndex();
    }
    /**
     * Sets the room's main node.
     * @param node the main node to set
     */
    public void setMainNode(int val) {
        mainNode = val;
    }
    public void print() {
        int width = this.getWidth();
        int height = this.getHeight();
        System.out.println(width+","+height);
    }
    private int getHeight() {
        int maxY = 0, minY = Integer.MAX_VALUE;
        for (int i = nodes.length - 1; i >= 0; i--) {
            maxY = Math.max(maxY, (int) nodes[i].getLocation().getY());
            minY = Math.min(minY, (int) nodes[i].getLocation().getY());
        }
        return maxY - minY + 1;
    }
    private int getWidth() {
        int maxX = 0, minX = Integer.MAX_VALUE;
        for (int i = nodes.length - 1; i >= 0; i--) {
            maxX = Math.max(maxX, (int) nodes[i].getLocation().getX());
            minX = Math.min(minX, (int) nodes[i].getLocation().getX());
        }
        return maxX - minX + 1;
    }
}
