package com.dalonedrow.module.ff.rpg;

/**
 * @author drau
 */
public enum Direction {
	/** EAST. */
	EAST(1, "EAST"),
	/** NORTH. */
	NORTH(0, "NORTH"),
	/** SOUTH. */
	SOUTH(2, "SOUTH"),
	/** WEST. */
	WEST(3, "WEST");
	/**
	 * Gets the {@link Direction} value of an int. If there is no corresponding
	 * value, null is returned.
	 * @param val the numeric value
	 * @return {@link Direction}
	 */
	public static Direction valueOf(final int val) {
		Direction d = null;
		switch (val) {
		case 0:
			d = NORTH;
			break;
		case 1:
			d = EAST;
			break;
		case 2:
			d = SOUTH;
			break;
		case 3:
			d = WEST;
			break;
		default:
			break;
		}
		return d;
	}
	/** the name. */
	private final String	name;
	/** the numeric value. */
	private final int		value;
	/**
	 * Creates a new instance of {@link Direction}.
	 * @param val the numeric value
	 * @param n the name
	 */
	Direction(final int val, final String n) {
		value = val;
		name = n;
	}
	/**
	 * Gets the name.
	 * @return {@link String}
	 */
	public String getName() {
		return name;
	}
	/**
	 * Gets the numeric value.
	 * @return {@link int}
	 */
	public int getValue() {
		return value;
	}
}
