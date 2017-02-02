package com.dalonedrow.module.ff.graph;

import java.util.LinkedList;

import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.module.ff.rpg.FFRoomNode;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.graph.DijkstraAlgorithm;
import com.dalonedrow.rpg.graph.DijkstraUndirectedSearch;
import com.dalonedrow.rpg.graph.EdgeWeightedUndirectedGraph;
import com.dalonedrow.rpg.graph.GraphNode;
import com.dalonedrow.rpg.graph.PhysicalGraphNode;
import com.dalonedrow.rpg.graph.WeightedGraphEdge;
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
        loaded = false;
    }
    /**
     * Adds a node to the world map.
     * @param node the {@link GraphNode}
     * @param roomNumber the room the node is a part of
     * @param isMain flag indicating whether the node is the room's main
     * @throws RPGException if an error occurs
     */
    public void addNode(final GraphNode node, final int roomNumber,
            final boolean isMain) throws RPGException {
        graph.addVertex(node);
        if (!hasRoom(roomNumber)) {
            rooms = ArrayUtilities.getInstance().extendArray(
                    new FFRoomNode(roomNumber), rooms);
        }
        getRoom(roomNumber).addNode(node, isMain);
        System.out.println("added node "+node.getIndex()+"::"+((PhysicalGraphNode)node).getLocation());
    }
    /**
     * Gets a room by its number
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
    public void getPath(final GraphNode source, GraphNode to) throws RPGException {
        /*
        DijkstraUndirectedSearch search =
                new DijkstraUndirectedSearch(graph, source.getIndex());
        WeightedGraphEdge[] edges = search.pathTo(to.getIndex());
        for (int i = 0, len = edges.length; i < len; i++) {
            WeightedGraphEdge edge = edges[i];
            PhysicalGraphNode from =
                    (PhysicalGraphNode) graph.getVertex(edge.getFrom());
            System.out.println(from.getLocation());
        }
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
     * Loads the world map.
     * @throws RPGException if an error occurs
     */
    public void load() throws RPGException {
        if (!loaded) {
            loaded = true;
            FFWebServiceClient.getInstance().loadMap();
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
                System.out.println("check NODE "+outer);
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
                    if ("CAVE_WALL".equalsIgnoreCase(new String(other.getName()))) {
                        continue;
                    }
                    if (other.getLocation().getX() == node.getLocation().getX()
                            && Math.abs((int) other.getLocation().getY()
                                    - (int) node.getLocation().getY()) == 1) {
                        System.out.println("\tadded edge from "+node.getIndex()+"::"+node.getLocation()+" to "
                                +other.getIndex()+"::"+other.getLocation());
                        graph.addEdge(node.getIndex(), other.getIndex());
                    } else if (other.getLocation().getY()
                            == node.getLocation().getY()
                            && Math.abs((int) other.getLocation().getX()
                                    - (int) node.getLocation().getX()) == 1) {
                        // add bi-directional edge
                        graph.addEdge(node.getIndex(), other.getIndex());
                        System.out.println("\tadded edge from "+node.getIndex()+"::"+node.getLocation()+" to "
                                +other.getIndex()+"::"+other.getLocation());
                    }
                }
            }
        }
    }
}
