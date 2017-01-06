package com.dalonedrow.module.ff.rpg;

/**
 * 
 * @author drau
 *
 */
public final class FFDoorData {
	/** flag indicating whether the door is locked. */
	private boolean	locked;
	/** the door's name. */
	private String	name;
	/** the door's title. */
	private String	title;
	/**
	 * Gets the door's name.
	 * @return {@link String}
	 */
	public String getName() {
		return name;
	}
	/**
	 * Gets the door's title.
	 * @return {@link String}
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * Gets the flag indicating whether the door is locked.
	 * @return {@link boolean}
	 */
	public boolean isLocked() {
		return locked;
	}
	/**
	 * Sets the flag indicating whether the door is locked.
	 * @param val the new value to set
	 */
	public void setLocked(final boolean val) {
		locked = val;
	}
	/**
	 * Sets the door's name.
	 * @param val the new value to set
	 */
	public void setName(final String val) {
		name = val;
	}
	/**
	 * Sets the door's title.
	 * @param val the new value to set
	 */
	public void setTitle(final String val) {
		title = val;
	}
}
