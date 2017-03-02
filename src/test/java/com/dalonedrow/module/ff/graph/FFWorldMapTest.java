package com.dalonedrow.module.ff.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.graph.GraphNode;

/**
 * 
 * @author 588648
 *
 */
public class FFWorldMapTest {
    /**
     * Perform these processes before testing.
     * @throws RPGException if an error occurs
     */
    @Before
    public void before() throws RPGException {
        FFWorldMap.getInstance().load();
    }
    /**
     * Tests that room 1 gets loaded.
     * @throws RPGException if an error occurs
     */
    @Test
    public void canGetRoom1() throws RPGException {
        FFRoomNode room = FFWorldMap.getInstance().getRoom(1);
        assertEquals("got room 1", 1, room.getId());
        assertTrue("room 1 main node is 641,1337",
                room.getMainNode().getLocation().equals(641, 1337));
        assertTrue("room 1 has node is 643,1338", room.hasNode(643, 1338));
        assertEquals("Node 640,1337 is floor", "CAVE_FLOOR",
                new String(room.getNode(640, 1337).getName()));
    }
    /**
     * Tests that room 12 gets loaded.
     * @throws RPGException if an error occurs
     */
    @Test
    public void canGetRoom12() throws RPGException {
        FFRoomNode room = FFWorldMap.getInstance().getRoom(12);
        assertEquals("got room 12", 12, room.getId());
        assertTrue("room 12 main node is 646,1337",
                room.getMainNode().getLocation().equals(646, 1337));
        assertTrue("room 12 has node is 648,1338", room.hasNode(648, 1338));
        assertEquals("Node 644,1337 is floor", "CAVE_FLOOR",
                new String(room.getNode(644, 1337).getName()));
    }
    /**
     * Tests that room 139 gets loaded.
     * @throws RPGException if an error occurs
     */
    @Test
    public void canGetRoom139() throws RPGException {
        FFRoomNode room = FFWorldMap.getInstance().getRoom(139);
        assertEquals("got room 139", 139, room.getId());
        assertTrue("room 139 main node is 652,1337",
                room.getMainNode().getLocation().equals(652, 1337));
        assertTrue("room 139 has node is 649,1338", room.hasNode(649, 1338));
        assertEquals("Node 652,1335 is floor", "CAVE_FLOOR",
                new String(room.getNode(652, 1335).getName()));
    }
    /**
     * Tests that a path exists between rooms 1 and 12.
     * @throws RPGException if an error occurs
     */
    @Test
    public void getPathFromRoom1ToRoom12() throws RPGException {
        FFWorldMap.getInstance().getRoom(1).setVisited(true);
        GraphNode mn1 = FFWorldMap.getInstance().getRoom(1).getMainNode();
        GraphNode mn12 = FFWorldMap.getInstance().getRoom(12).getMainNode();
        assertTrue(FFWorldMap.getInstance().hasPath(mn1, mn12));
    }
    /**
     * Tests that a path exists between rooms 12 and 1.
     * @throws RPGException if an error occurs
     */
    @Test
    public void getPathFromRoom12ToRoom1() throws RPGException {
        FFWorldMap.getInstance().getRoom(12).setVisited(true);
        GraphNode node1 = FFWorldMap.getInstance().getRoom(12).getMainNode();
        GraphNode node2 = FFWorldMap.getInstance().getRoom(1).getMainNode();
        assertTrue(FFWorldMap.getInstance().hasPath(node1, node2));
    }
    /**
     * Tests that a path exists between rooms 12 and 139.
     * @throws RPGException if an error occurs
     */
    @Test
    public void getPathFromRoom12ToRoom139() throws RPGException {
        FFWorldMap.getInstance().getRoom(12).setVisited(true);
        GraphNode mn1 = FFWorldMap.getInstance().getRoom(12).getMainNode();
        GraphNode mn12 =
                FFWorldMap.getInstance().getRoom(139).getNode(650, 1337);
        assertTrue(FFWorldMap.getInstance().hasPath(mn1, mn12));
    }
    /**
     * Tests that a path exists between rooms 139 and 12.
     * @throws RPGException if an error occurs
     */
    @Test
    public void getPathFromRoom139ToRoom12() throws RPGException {
        FFWorldMap.getInstance().getRoom(139).setVisited(true);
        GraphNode node1 =
                FFWorldMap.getInstance().getRoom(139).getNode(650, 1337);
        GraphNode node2 = FFWorldMap.getInstance().getRoom(12).getMainNode();
        assertTrue(FFWorldMap.getInstance().hasPath(node1, node2));
    }
}
