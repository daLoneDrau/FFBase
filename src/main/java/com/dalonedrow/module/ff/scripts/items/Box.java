package com.dalonedrow.module.ff.scripts.items;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.rpg.base.flyweights.RPGException;

/**
 * Iron sword.
 * @author drau
 */
@SuppressWarnings("unchecked")
public final class Box extends WeaponScript {
    /**
     * Creates a new instance of {@link Box}.
     * @param io the IO associated with the script
     */
    public Box(final FFInteractiveObject io) {
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
        super.setLocalVariable("poisonable", 0);
        return super.onInit();
    }
    @Override
    public int onInitEnd() throws RPGException {
        String name = new String(super.getIO().getNPCData().getName());
        if ("BOX_1".equalsIgnoreCase(name)) {
            super.getIO().setPosition(new SimpleVector2(632, 1332));
        }
        name = null;
        super.getIO().setShow(1);
        return super.onInitEnd();
    }
}
