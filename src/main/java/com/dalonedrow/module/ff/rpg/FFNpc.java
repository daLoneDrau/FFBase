package com.dalonedrow.module.ff.rpg;

import java.util.HashMap;
import java.util.Map;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.module.ff.systems.wofm.FFWoFMController;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.rpg.base.constants.EquipmentGlobals;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.constants.ScriptConsts;
import com.dalonedrow.rpg.base.flyweights.Attribute;
import com.dalonedrow.rpg.base.flyweights.IoNpcData;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.Script;

/**
 * @author drau
 */
public final class FFNpc extends IoNpcData<FFInteractiveObject> {
	/**
	 * Creates a new instance of {@link FFNpc}.
	 * @throws RPGException if an error occurs
	 */
	public FFNpc() throws RPGException {
		super();
		// TODO Auto-generated constructor stub
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
	public void ARX_NPC_ManagePoison() {
		// TODO Auto-generated method stub

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
		super.setAttributes(map);
		map = null;
	}
	public void doComputeFullStats() {
		// TODO Auto-generated method stub
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getBaseLife() {
		doComputeFullStats();
		return super.getBaseAttributeScore("ST");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getBaseMana() {
		// TODO Auto-generated method stub
		doComputeFullStats();
		return 0;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean hasLifeRemaining() {
		return super.getBaseAttributeScore("ST") > 0f;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void moveToInitialPosition() {
		// TODO Auto-generated method stub

	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void restoreLifeToMax() {
		super.setBaseAttributeScore("ST", super.getBaseAttributeScore("MST"));
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void stopActiveAnimation() {
		// TODO Auto-generated method stub

	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void stopIdleAnimation() {
		// TODO Auto-generated method stub

	}
}
