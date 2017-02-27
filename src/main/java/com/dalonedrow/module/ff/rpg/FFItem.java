package com.dalonedrow.module.ff.rpg;

import com.dalonedrow.rpg.base.flyweights.IOEquipItem;
import com.dalonedrow.rpg.base.flyweights.IOItemData;

/**
 * @author drau
 */
public final class FFItem extends IOItemData<FFInteractiveObject> {
    /** Creates a new instance of {@link FFItem}. */
    public FFItem() {
        super.setEquipitem(new IOEquipItem());
    }

    @Override
    protected float applyCriticalModifier() {
        return 0;
    }

    @Override
    protected float calculateArmorDeflection() {
        return 0;
    }

    @Override
    protected float getBackstabModifier() {
        return 0;
    }
}
