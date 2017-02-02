package com.dalonedrow.module.ff.graph;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.dalonedrow.module.ff.rpg.FFRoomNode;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.graph.GraphNode;

public class FFWorldMapTest {
    @Before
    public void before() throws RPGException {
        FFWorldMap.getInstance().load();
    }
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
    @Test
    public void canGetRoom12() throws RPGException {
        FFRoomNode room = FFWorldMap.getInstance().getRoom(12);
        assertEquals("got room 12", 12, room.getId());
        assertTrue("room 12 main node is 646,1337",
                room.getMainNode().getLocation().equals(646, 1337));
        assertTrue("room 278 has node is 648,1338", room.hasNode(648, 1338));
        assertEquals("Node 644,1337 is floor", "CAVE_FLOOR",
                new String(room.getNode(644, 1337).getName()));
    }
    @Test
    public void getPathFromRoom1ToRoom12() throws RPGException {
        GraphNode mn1 = FFWorldMap.getInstance().getRoom(1).getMainNode();
        GraphNode mn12 = FFWorldMap.getInstance().getRoom(12).getMainNode();
        FFWorldMap.getInstance().getPath(mn1, mn12);
        FFWorldMap.getInstance().getRoom(1).print();
    }
}
