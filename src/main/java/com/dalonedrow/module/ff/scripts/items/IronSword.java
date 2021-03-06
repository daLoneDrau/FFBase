package com.dalonedrow.module.ff.scripts.items;

import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.rpg.base.flyweights.RPGException;

/**
 * Iron sword.
 * @author drau
 */
@SuppressWarnings("unchecked")
public final class IronSword extends WeaponScript {
    /**
     * Creates a new instance of {@link IronSword}.
     * @param io the IO associated with the script
     */
    public IronSword(final FFInteractiveObject io) {
        super(io);
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onInit()
     */
    @Override
    public int onInit() throws RPGException {
        // set local variables
        super.setLocalVariable("reagent", "none");
        super.setLocalVariable("poisonable", 1);
        return super.onInit();
    }
}
