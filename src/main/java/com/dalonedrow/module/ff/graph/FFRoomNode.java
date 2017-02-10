package com.dalonedrow.module.ff.graph;

import java.util.ArrayList;
import java.util.List;

import com.dalonedrow.module.ff.rpg.FFCommand;
import com.dalonedrow.rpg.graph.GraphNode;
import com.dalonedrow.rpg.graph.PhysicalGraphNode;
import com.dalonedrow.utils.ArrayUtilities;

/**
 * 
 * @author 588648
 *
 */
public class FFRoomNode {
    /** the room's id. */
    private final int id;
    /** the index of the room's main node. */
    private int mainNode;
    /** the list of {@link GraphNode}s the room contains. */
    private PhysicalGraphNode[] nodes;
    /**
     * the flag indicating whether the initial text has been displayed.
     */
    private boolean initialTextDisplayed;
    /**
     * the flag indicating whether the player has visited the room before or
     * not.
     */
    private boolean visited;
    /**
     * Gets the flag indicating whether the player has visited the room before
     * or not.
     * @return <tt>true</tt> if the player has visited the room; <tt>false</tt>
     * otherwise
     */
    public boolean isVisited() {
        return visited;
    }
    /**
     * Sets the flag indicating whether the player has visited the room before
     * or not.
     * @param flag the flag
     */
    public void setVisited(final boolean flag) {
        this.visited = flag;
    }
    /**
     * Creates a new instance of {@link FFRoomNode}.
     * @param roomId the room's id
     */
    public FFRoomNode(final int roomId) {
        id = roomId;
        mainNode = -1;
        nodes = new PhysicalGraphNode[0];
        commands = new ArrayList<FFCommand>();
    }
    /**
     * Adds a {@link GraphNode} to the room.
     * @param node the {@link GraphNode}
     * @param isMain the flag indicating whether the node is the main node
     */
    public void addNode(final GraphNode node, final boolean isMain) {
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
    public boolean contains(final GraphNode node) {
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
     * Gets the main node.
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
     * @param index the {@link GraphNode}'s index
     * @return <tt>true</tt> if the room has the node; <tt>false</tt> otherwise
     */
    private boolean hasNode(final int index) {
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
     * 
     * Determines if a room contains a specific {@link GraphNode}.
     * @param x the node's x-coordinates
     * @param y the node's y-coordinates
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
     * @param index the main node's index to set
     */
    public void setMainNode(final int index) {
        mainNode = index;
    }
    private List<FFCommand> commands;
    public void addCommand(final FFCommand ffCommand) {
        if (ffCommand != null
                && !commands.contains(ffCommand)) {
            commands.add(ffCommand);
        }
    }
    public FFCommand[] getCommands() {
        return commands.toArray(new FFCommand[commands.size()]);
    }
}
