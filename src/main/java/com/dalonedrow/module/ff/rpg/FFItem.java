package com.dalonedrow.module.ff.rpg;

import com.dalonedrow.rpg.base.flyweights.IOEquipItem;
import com.dalonedrow.rpg.base.flyweights.IOItemData;
import com.dalonedrow.rpg.base.flyweights.RPGException;

/**
 * @author drau
 */
public final class FFItem extends IOItemData<FFInteractiveObject> {
	/** Creates a new instance of {@link FFItem}. */
	public FFItem() {
		super.setEquipitem(new IOEquipItem());
	}
}
