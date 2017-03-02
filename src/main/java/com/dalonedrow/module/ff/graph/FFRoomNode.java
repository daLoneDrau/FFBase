package com.dalonedrow.module.ff.graph;

import java.util.ArrayList;
import java.util.List;

import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFCommand;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.graph.GraphNode;
import com.dalonedrow.rpg.graph.PhysicalGraphNode;
import com.dalonedrow.utils.ArrayUtilities;

/**
 * @author 588648
 */
public class FFRoomNode {
    private List<FFCommand> commands;
    private char[] displayText;
    /** the room's id. */
    private final int id;
    /**
     * the flag indicating whether the initial text has been displayed.
     */
    private boolean initialTextDisplayed;
    /** the index of the room's main node. */
    private int mainNode;
    /** the list of {@link GraphNode}s the room contains. */
    private PhysicalGraphNode[] nodes;
    /**
     * the flag indicating whether the player has visited the room before or
     * not.
     */
    private boolean visited;
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
     * Adds a command to the list of available choices.
     * @param command the new {@link FFCommand}
     */
    public void addCommand(final FFCommand command) {
        if (command != null
                && !commands.contains(command)) {
            commands.add(command);
        }
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
    public FFCommand[] getCommands() {
        return commands.toArray(new FFCommand[commands.size()]);
    }
    /**
     * @return the displayText
     */
    public String getDisplayText() {
        return new String(displayText);
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
     * Gets all nodes in the room.
     * @return {@link PhysicalGraphNode}[]
     */
    public PhysicalGraphNode[] getNodes() {
        return nodes;
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
     * Gets the flag indicating whether the player has visited the room before
     * or not.
     * @return <tt>true</tt> if the player has visited the room; <tt>false</tt>
     *         otherwise
     */
    public boolean isVisited() {
        return visited;
    }
    /**
     * Removes a command to the list of available choices.
     * @param command the old {@link FFCommand}
     */
    public void removeCommand(final FFCommand command) {
        if (command != null
                && commands.contains(command)) {
            commands.remove(command);
        }
    }
    /**
     * @param displayText the displayText to set
     */
    public void setDisplayText(String text) {
        displayText = text.toCharArray();
    }
    /**
     * @param initialTextDisplayed the initialTextDisplayed to set
     * @throws RPGException if an error occurs setting the secondary display
     *             text
     */
    public void setInitialTextDisplayed(final boolean flag)
            throws RPGException {
        initialTextDisplayed = flag;
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append(id);
            sb.append("_SECONDARY");
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        try {
            String s = FFWebServiceClient.getInstance().loadText(sb.toString());
            displayText = s.toCharArray();
        } catch (RPGException e) {
            if (e.getErrorMessage() != ErrorMessage.INVALID_DATA_FORMAT) {
                throw e;
            }
        }
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
    /**
     * Sets the flag indicating whether the player has visited the room before
     * or not.
     * @param flag the flag
     */
    public void setVisited(final boolean flag) {
        visited = flag;
    }
    /**
     * @return the initialTextDisplayed
     */
    public boolean wasInitialTextDisplayed() {
        return initialTextDisplayed;
    }
}
