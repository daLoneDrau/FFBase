package com.dalonedrow.module.ff.graph;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.dalonedrow.engine.sprite.base.SimplePoint;
import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.engine.systems.base.Diceroller;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.module.ff.systems.FFInteractive;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.graph.DijkstraAlgorithm;
import com.dalonedrow.rpg.graph.EdgeWeightedUndirectedGraph;
import com.dalonedrow.rpg.graph.GraphNode;
import com.dalonedrow.rpg.graph.PhysicalGraphNode;
import com.dalonedrow.utils.ArrayUtilities;

public class FFWorldMap {
    /** the one and only instance of the <tt>Script</tt> class. */
    private static FFWorldMap instance;
    /**
     * Gives access to the singleton instance of {@link FFWorldMap}.
     * @return {@link FFWorldMap}
     * @throws RPGException if an error occurs
     */
    public static FFWorldMap getInstance() throws RPGException {
        if (FFWorldMap.instance == null) {
            FFWorldMap.instance = new FFWorldMap();
        }
        return FFWorldMap.instance;
    }
    /** the comparator for organizing the viewport. */
    private ViewportComparator comparator;
    /** the map's graph. */
    private EdgeWeightedUndirectedGraph graph;
    /** flag indicating whether the map has been loaded. */
    private boolean loaded;
    /** the list of rooms. */
    private FFRoomNode[] rooms;
    /**
     * Hidden constructor.
     * @throws RPGException if an error occurs
     */
    private FFWorldMap() throws RPGException {
        graph = new EdgeWeightedUndirectedGraph(0);
        rooms = new FFRoomNode[0];
        comparator = new ViewportComparator();
        loaded = false;
    }
    /**
     * Adds a node to the world map.
     * @param node the {@link GraphNode}
     * @param roomNumber the room the node is a part of
     * @param isMain flag indicating whether the node is the room's main
     * @throws RPGException if an error occurs
     */
    public void addNode(final FFMapNode node, final int roomNumber,
            final boolean isMain) throws RPGException {
        graph.addVertex(node);
        if (!hasRoom(roomNumber)) {
            rooms = ArrayUtilities.getInstance().extendArray(
                    new FFRoomNode(roomNumber), rooms);
        }
        getRoom(roomNumber).addNode(node, isMain);
        node.setRoomNumber(roomNumber);
    }
    public void getPath(final GraphNode source, GraphNode to)
            throws RPGException {
        /*
         * DijkstraUndirectedSearch search = new DijkstraUndirectedSearch(graph,
         * source.getIndex()); WeightedGraphEdge[] edges =
         * search.pathTo(to.getIndex()); for (int i = 0, len = edges.length; i <
         * len; i++) { WeightedGraphEdge edge = edges[i]; PhysicalGraphNode from
         * = (PhysicalGraphNode) graph.getVertex(edge.getFrom());
         * System.out.println(from.getLocation()); }
         */
        DijkstraAlgorithm search = new DijkstraAlgorithm(graph);
        search.execute(source);
        LinkedList<GraphNode> l = search.getPath(to);
        for (int i = 0, len = l.size(); i < len; i++) {
            PhysicalGraphNode from = (PhysicalGraphNode) l.get(i);
            System.out.println(from.getLocation());
        }
    }
    /**
     * Gets the room the player is occupying.
     * @return {@link FFRoomNode}
     * @throws RPGException if an error occurs
     */
    public FFRoomNode getPlayerRoom() throws RPGException {
        return this.getRoomByCellCoordinates(
                ((FFController) ProjectConstants.getInstance())
                        .getPlayerIO().getPosition());
    }
    /**
     * Gets a room by its number.
     * @param roomNumber the room number
     * @return {@link FFRoomNode}
     */
    public FFRoomNode getRoom(final int roomNumber) {
        FFRoomNode room = null;
        if (hasRoom(roomNumber)) {
            for (int i = rooms.length - 1; i >= 0; i--) {
                if (rooms[i].getId() == roomNumber) {
                    room = rooms[i];
                    break;
                }
            }
        }
        return room;
    }
    /**
     * Gets the room associated with a specific coordinate.
     * @param coordinate the coordinates
     * @return {@link FFRoomNode}
     */
    public FFRoomNode getRoomByCellCoordinates(final SimplePoint coordinate) {
        FFRoomNode room = null;
        GraphNode[] nodes = graph.getVertexes();
        for (int i = nodes.length - 1; i >= 0; i--) {
            FFMapNode node = (FFMapNode) nodes[i];
            if (node.equals(coordinate)) {
                room = getRoom(node.getRoomNumber());
                break;
            }
        }
        return room;
    }
    /**
     * Gets the room associated with a specific coordinate.
     * @param coordinate the coordinates
     * @return {@link FFRoomNode}
     */
    public FFRoomNode getRoomByCellCoordinates(final SimpleVector2 coordinate) {
        FFRoomNode room = null;
        GraphNode[] nodes = graph.getVertexes();
        for (int i = nodes.length - 1; i >= 0; i--) {
            FFMapNode node = (FFMapNode) nodes[i];
            if (node.equals(coordinate)) {
                room = getRoom(node.getRoomNumber());
                break;
            }
        }
        return room;
    }
    /**
     * Determines if the map has a room by a specific number.
     * @param roomNumber the room number
     * @return <tt>true</tt> if the world map has the room, <tt>false</tt>
     *         otherwise
     */
    public boolean hasRoom(final int roomNumber) {
        boolean has = false;
        for (int i = rooms.length - 1; i >= 0; i--) {
            if (rooms[i].getId() == roomNumber) {
                has = true;
                break;
            }
        }
        return has;
    }
    /**
     * Loads the world map.
     * @throws RPGException if an error occurs
     */
    public void load() throws RPGException {
        if (!loaded) {
            loaded = true;
            FFWebServiceClient.getInstance().loadMap();
            // update rooms with commands
            for (int i = rooms.length - 1; i >= 0; i--) {
                FFWebServiceClient.getInstance().loadRoomData(rooms[i]);
            }
            // connect all vertices
            for (int outer = graph.getNumberOfVertices(); outer >= 0; outer--) {
                if (graph.getVertex(outer) == null) {
                    continue;
                }
                PhysicalGraphNode node =
                        (PhysicalGraphNode) graph.getVertex(outer);
                if ("CAVE_WALL".equalsIgnoreCase(new String(node.getName()))) {
                    continue;
                }
                for (int inner = graph.getNumberOfVertices(); inner >= 0;
                        inner--) {
                    if (graph.getVertex(inner) == null) {
                        continue;
                    }
                    if (graph.getVertex(inner).equals(node)) {
                        continue;
                    }
                    PhysicalGraphNode other =
                            (PhysicalGraphNode) graph.getVertex(inner);
                    if ("CAVE_WALL"
                            .equalsIgnoreCase(new String(other.getName()))) {
                        continue;
                    }
                    if (other.getLocation().getX() == node.getLocation().getX()
                            && Math.abs((int) other.getLocation().getY()
                                    - (int) node.getLocation().getY()) == 1) {
                        graph.addEdge(node.getIndex(), other.getIndex());
                    } else if (other.getLocation().getY() == node.getLocation()
                            .getY()
                            && Math.abs((int) other.getLocation().getX()
                                    - (int) node.getLocation().getX()) == 1) {
                        // add bi-directional edge
                        graph.addEdge(node.getIndex(), other.getIndex());
                    }
                }
            }
        }
    }
    public String renderViewport() {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        SimplePoint pt = getRoom(1).getMainNode().getLocation();
        int maxX = (int) (pt.getX() + 16), minX = (int) (pt.getX() - 16);
        int maxY = (int) (pt.getY() + 5), minY = (int) (pt.getY() - 5);
        minX = Math.max(minX, 0);
        maxY = Math.min(maxY, 1340);
        Rectangle r = new Rectangle(minX, minY, 33, 11);
        // find all nodes within the viewport
        List<FFMapNode> view = new ArrayList<FFMapNode>();
        GraphNode[] nodes = graph.getVertexes();
        for (int i = nodes.length - 1; i >= 0; i--) {
            FFMapNode node = (FFMapNode) nodes[i];
            if (r.contains(node.getLocation().getX(),
                    node.getLocation().getY())) {
                FFRoomNode room = getRoom(node.getRoomNumber());
                if (room.isVisited()) {
                    view.add(node);
                }
            }
        }
        Collections.sort(view, comparator);
        int i = 0;
        PhysicalGraphNode node = view.get(i);
        try {
            for (int row = minY; row <= maxY; row++) {
                for (int col = minX; col <= maxX; col++) {
                    if ((int) node.getLocation().getY() > row) {
                        // node is not on the same row.
                        // print blank space
                        sb.append("   ");
                    } else if ((int) node.getLocation().getY() == row) {
                        // node is on the same row.
                        // is node being rendered?
                        if ((int) node.getLocation().getX() == col) {
                            // render node
                            if ("CAVE_WALL"
                                    .equalsIgnoreCase(
                                            new String(node.getName()))) {
                                sb.append("###");
                            } else {
                                // this is a floor
                                FFInteractiveObject io =
                                        ((FFInteractive) Interactive
                                                .getInstance())
                                                        .getIoAtPosition(node
                                                                .getLocation());
                                if (io != null) {
                                    if (io.hasIOFlag(IoGlobals.IO_01_PC)) {
                                        sb.append(" @ ");
                                    } else if (io
                                            .hasIOFlag(IoGlobals.IO_03_NPC)) {
                                        sb.append(' ');
                                        sb.append(Character.toUpperCase(
                                                io.getNPCData().getTitle()[0]));
                                        sb.append(' ');
                                    } else if (io
                                            .hasIOFlag(IoGlobals.IO_02_ITEM)) {
                                        sb.append(' ');
                                        sb.append(Character.toUpperCase(
                                                io.getItemData().getTitle()
                                                        .charAt(0)));
                                        sb.append(' ');
                                    }
                                } else {
                                    // TODO - check for IOs in node
                                    for (int f = 3; f > 0; f--) {
                                        switch (Diceroller.getInstance()
                                                .rolldX(10)) {
                                        case 1:
                                        case 2:
                                        case 3:
                                        case 4:
                                        case 5:
                                            sb.append(' ');
                                            break;
                                        case 6:
                                            sb.append(',');
                                            break;
                                        case 7:
                                            sb.append('`');
                                            break;
                                        case 8:
                                            sb.append(';');
                                            break;
                                        case 9:
                                            sb.append('\'');
                                            break;
                                        case 10:
                                            sb.append('.');
                                            break;
                                        }
                                    }
                                }
                            }
                            // get next node
                            if (++i < view.size()) {
                                node = view.get(i);
                            }
                        } else {
                            // node is not the same col.
                            // print blank space
                            sb.append("   ");
                        }
                    }
                    // if row ends, println
                    if (col == maxX) {
                        sb.append('\n');
                    }
                }
            }
        } catch (PooledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String s = sb.toString();
        sb.returnToPool();
        sb = null;
        return s;
    }
}

/**
 * Utility class to sort nodes for the viewport.
 * @author 588648
 */
class ViewportComparator implements Comparator<PhysicalGraphNode> {
    @Override
    public int compare(PhysicalGraphNode o1, PhysicalGraphNode o2) {
        int comparison = 0;
        if (o1.getLocation().getY() < o2.getLocation().getY()) {
            comparison = -1;
        } else if (o1.getLocation().getY() > o2.getLocation().getY()) {
            comparison = 1;
        } else {
            if (o1.getLocation().getX() < o2.getLocation().getX()) {
                comparison = -1;
            } else if (o1.getLocation().getX() > o2.getLocation().getX()) {
                comparison = 1;
            }
        }
        return comparison;
    }

}
