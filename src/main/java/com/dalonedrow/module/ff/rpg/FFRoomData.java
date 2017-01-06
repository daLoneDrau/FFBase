package com.dalonedrow.module.ff.rpg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.JOGLErrorHandler;
import com.dalonedrow.module.ff.constants.FFIo;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.utils.ArrayUtilities;
import com.dalonedrow.utils.Watchable;
import com.dalonedrow.utils.Watcher;

/**
 * @author drau
 */
public final class FFRoomData implements Watchable {
	/** the room's reference id. */
	private int							code;
	/** the list of doors in the room. */
	private int[]						doors;
	/** the room's exits. */
	private final Map<Integer, Integer>	exits;
	/** the room's text reference id. */
	private String						textId;
	/** the list of watchers. */
	private final List<Watcher>			watchers;
	/** Creates a new instance of {@link FFRoomData}. */
	public FFRoomData() {
		doors = new int[0];
		exits = new HashMap<Integer, Integer>();
		watchers = new ArrayList<Watcher>();
	}
	/**
	 * Adds a door to the room.
	 * @param io the door's reference id
	 */
	public void addDoor(final FFInteractiveObject io) {
		if (io != null
				&& io.hasIOFlag(FFIo.IO_18_DOOR)
				&& io.getDoorData() != null) {
			doors = ArrayUtilities.getInstance().extendArray(
					io.getRefId(), doors);
		}
	}
	/**
	 * Adds a door to the room.
	 * @param ioid the door's reference id
	 * @throws RPGException if an error occurs
	 */
	public void addDoor(final int ioid) throws RPGException {
		addDoor((FFInteractiveObject) Interactive.getInstance().getIO(ioid));
	}
	/**
	 * Adds an exit to the room.
	 * @param direction the exit direction
	 * @param destination the exit destination
	 */
	public void addExit(final int direction, final int destination) {
		exits.put(direction, destination);
	}
	@Override
	public void addWatcher(final Watcher watcher) {
		if (!watchers.contains(watcher)) {
			watchers.add(watcher);
		}
	}
	/**
	 * Determines if any of the doors in a room can be bashed.
	 * @return <tt>true</tt> if there are any doors to bash; <tt>false</tt>
	 *         otherwise.
	 */
	public boolean canBashDoorsInRoom() {
		int[] lockedDoors = getLockedDoors();
		boolean canBash = false;
		for (int i = lockedDoors.length - 1; i >= 0; i--) {
			try {
				FFInteractiveObject io =
						(FFInteractiveObject) Interactive.getInstance()
								.getIO(lockedDoors[i]);
				if (io.getScript()
						.getLocalIntVariableValue("perma_locked") == 0) {
					canBash = true;
					break;
				}
			} catch (RPGException e) {
				JOGLErrorHandler.getInstance().fatalError(e);
			}
		}
		return canBash;
	}
	/**
	 * Gets the room's reference id.
	 * @return {@link int}
	 */
	public int getCode() {
		return code;
	}
	/**
	 * Gets the code for the room an exit leads to.
	 * @param dir the exit direction
	 * @return {@link int}
	 */
	public int getExit(final int dir) {
		int exit = -1;
		if (hasExit(dir)) {
			exit = exits.get(dir);
		}
		return exit;
	}
	/**
	 * Gets the list of exits for this room.
	 * @return {@link String}[]
	 */
	public String[] getExitDirections() {
		String[] list = new String[0];
		Iterator<Integer> iter = exits.keySet().iterator();
		while (iter.hasNext()) {
			int exit = iter.next();
			Direction d = Direction.valueOf(exit);
			if (d == null) {
				JOGLErrorHandler.getInstance().fatalError(new RPGException(
						ErrorMessage.INTERNAL_BAD_ARGUMENT, "Room " + code
								+ " has Invalid exit to direction " + exit));
			}
			list = ArrayUtilities.getInstance().extendArray(d.getName(), list);
			d = null;
		}
		return list;
	}
	/**
	 * Gets the list of locked doors in a room.
	 * @return {@link int}[]
	 */
	public int[] getLockedDoors() {
		int[] arr = new int[0];
		if (doors.length > 0) {
			for (int i = doors.length - 1; i >= 0; i--) {
				try {
					FFInteractiveObject io =
							(FFInteractiveObject) Interactive.getInstance()
									.getIO(doors[i]);
					if (io.getDoorData().isLocked()) {
						arr = ArrayUtilities.getInstance().extendArray(
								io.getRefId(), arr);
					}
				} catch (RPGException e) {
					e.printStackTrace();
				}
			}
		}
		return arr;
	}
	/**
	 * Gets the number of locked doors in a room.
	 * @return {@link int}
	 */
	public int getNumLockedDoors() {
		int count = 0;
		if (doors.length > 0) {
			for (int i = doors.length - 1; i >= 0; i--) {
				try {
					FFInteractiveObject io =
							(FFInteractiveObject) Interactive.getInstance()
									.getIO(doors[i]);
					if (io.getDoorData().isLocked()) {
						count++;
					}
				} catch (RPGException e) {
					e.printStackTrace();
				}
			}
		}
		return count;
	}
	/**
	 * Gets the room's text reference id.
	 * @return {@link String}
	 */
	public String getTextid() {
		return textId;
	}
	/**
	 * Determines if the room has an exit in a specific direction.
	 * @param dir the specific direction
	 * @return <tt>true</tt> if the room has an exit in that direction;
	 *         <tt>false</tt> otherwise
	 */
	public boolean hasExit(final Direction dir) {
		return hasExit(dir.getValue());
	}
	/**
	 * Determines if the room has an exit in a specific direction.
	 * @param dir the specific direction
	 * @return <tt>true</tt> if the room has an exit in that direction;
	 *         <tt>false</tt> otherwise
	 */
	public boolean hasExit(final int dir) {
		return exits.get(dir) != null;
	}
	/**
	 * Determines if the room has any locked doors.
	 * @return <tt>true</tt> if the room has at least one locked door;
	 *         <tt>false</tt> otherwise
	 */
	public boolean hasLockedDoors() {
		boolean has = false;
		if (doors.length > 0) {
			for (int i = doors.length - 1; i >= 0; i--) {
				try {
					FFInteractiveObject io =
							(FFInteractiveObject) Interactive.getInstance()
									.getIO(doors[i]);
					if (io.getDoorData().isLocked()) {
						has = true;
						break;
					}
				} catch (RPGException e) {
					e.printStackTrace();
				}
			}
		}
		return has;
	}
	@Override
	public void notifyWatchers() {
		for (int i = watchers.size() - 1; i >= 0; i--) {
			watchers.get(i).watchUpdated(this);
		}
	}
	/**
	 * Removes a door that has been bashed down.
	 * @param name the name of the door
	 */
	public void removeDoor(final String name) {
		int index = -1;
		if (doors.length > 0) {
			for (int i = doors.length - 1; i >= 0; i--) {
				try {
					FFInteractiveObject io =
							(FFInteractiveObject) Interactive.getInstance()
									.getIO(doors[i]);
					if (io.getDoorData().getName().equalsIgnoreCase(name)) {
						index = i;
						break;
					}
				} catch (RPGException e) {
					e.printStackTrace();
				}
			}
		}
		if (index >= 0) {
			doors = ArrayUtilities.getInstance().removeIndex(index, doors);
		}
	}
	@Override
	public void removeWatcher(final Watcher watcher) {
		watchers.remove(watcher);
	}
	/**
	 * Sets the room's reference id.
	 * @param id the new value to set
	 */
	public void setCode(final int id) {
		code = id;
	}
	/**
	 * Sets the room's text reference id.
	 * @param id the new value to set
	 */
	public void setTextid(final String id) {
		textId = id;
		notifyWatchers();
	}
}
