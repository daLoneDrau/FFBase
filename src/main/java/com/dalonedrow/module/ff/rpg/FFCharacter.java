package com.dalonedrow.module.ff.rpg;

import java.util.HashMap;
import java.util.Map;

import com.dalonedrow.engine.systems.base.Diceroller;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.systems.wofm.FFGlobals;
import com.dalonedrow.module.ff.systems.wofm.FFSpell;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.constants.Dice;
import com.dalonedrow.rpg.base.flyweights.Attribute;
import com.dalonedrow.rpg.base.flyweights.IOEquipItem;
import com.dalonedrow.rpg.base.flyweights.IoPcData;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.Spell;

/**
 * @author drau
 */
@SuppressWarnings("unchecked")
public final class FFCharacter extends IoPcData<FFInteractiveObject> {
	/** flag indicating pretty printing has been turned on. */
	private boolean	prettyPrinting;
	private boolean	REFUSE_GAME_RETURN;
	/**
	 * Creates a new instance of {@link FFCharacter}.
	 * @throws RPGException if an error occurs
	 */
	public FFCharacter() throws RPGException {
		super();
	}
	/**
	 * Tests the player's luck.
	 * @param isCombat if <tt>true</tt>, this is tested during combat, and the
	 * player's Luck score will be lowered by 1, no matter the outcome
	 * @return if <tt>true</tt> the player passes the luck test;
	 * otherwise they fail
	 * @throws RPGException if an error occurs
	 */
	public boolean testYourLuck(final boolean isCombat) throws RPGException {
		boolean passed = false;
		doComputeFullStats();
		// roll 2 dice. if that is less than equal to Luck, the test passed
		int roll = Diceroller.getInstance().rollXdY(2, Dice.D6);
		int score = (int) super.getFullAttributeScore("LK");
		if (false) {
		//if (roll <= score) {
			passed = true;
		}
		if (isCombat) {
			// remove one luck point
			super.setBaseAttributeScore(
					"LK", super.getBaseAttributeScore("LK") - 1);
		}
		return passed;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void adjustLife(final float dmg) {
		super.setBaseAttributeScore("ST",
				super.getBaseAttributeScore("ST") + dmg);
		if (super.getBaseAttributeScore(
				"ST") > super.getFullAttributeScore("MST")) {
			// if Stamina now > max
			super.setBaseAttributeScore("ST",
					super.getFullAttributeScore("MST"));
		}
		if (super.getBaseAttributeScore("ST") < 0f) {
			// if Stamina now < 0
			super.setBaseAttributeScore("ST", 0f);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void adjustMana(final float dmg) {
		// TODO Auto-generated method stub

	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ARX_EQUIPMENT_RecreatePlayerMesh() {
		// TODO Auto-generated method stub

	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canIdentifyEquipment(final IOEquipItem equipitem) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void computeFullStats() throws RPGException {
		// set attribute modifiers
		Attribute attr = super.getAttribute("ST");
		// find attribute modifiers for stamina
		attr = null;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void defineAttributes() throws RPGException {
		Map<String, Attribute> map = new HashMap<String, Attribute>();
		map.put("ST", new Attribute("ST", "Stamina"));
		map.put("MST", new Attribute("MST", "Max Stamina"));
		map.put("SK", new Attribute("SK", "Skill"));
		map.put("MSK", new Attribute("MSK", "Max Skill"));
		map.put("LK", new Attribute("LK", "Luck"));
		map.put("MLK", new Attribute("MLK", "Max Luck"));
		super.setAttributes(map);
		map = null;
	}
	/**
	 * Triggers a computation of the full stats.
	 * @throws RPGException if an error occurs
	 */
	public void doComputeFullStats() throws RPGException {
		computeFullStats();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected float getBaseLife() {
		return super.getAttribute("ST").getBase();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected float getBaseMana() {
		// TODO Auto-generated method stub
		return 0;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInCombat() {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * Sets the value of the flag indicating pretty printing has been turned on.
	 * @param flag the new value to set
	 */
	public void setPrettyPrinting(final boolean flag) {
		prettyPrinting = flag;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = null;
		try {
			PooledStringBuilder sb =
					StringBuilderPool.getInstance().getStringBuilder();
			if (prettyPrinting) {
				sb.append(new String(super.getName()));
				sb.append('\n');
				sb.append("SK: ");
				sb.append((int) super.getFullAttributeScore("SK"));
				sb.append('/');
				sb.append((int) super.getFullAttributeScore("MSK"));
				sb.append(System.lineSeparator());
				sb.append("ST: ");
				sb.append((int) super.getFullAttributeScore("ST"));
				sb.append('/');
				sb.append((int) super.getFullAttributeScore("MST"));
				sb.append(System.lineSeparator());
				sb.append("LK: ");
				sb.append((int) super.getFullAttributeScore("LK"));
				sb.append('/');
				sb.append((int) super.getFullAttributeScore("MLK"));
				sb.append(System.lineSeparator());
				prettyPrinting = false;
			} else {
			}
			s = sb.toString();
			sb.returnToPool();
			sb = null;
		} catch (PooledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
}
