package com.dalonedrow.module.ff.scripts.npcs;

import com.dalonedrow.engine.sprite.base.SimpleVector2;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.rpg.base.flyweights.RPGException;

/**
 * @author drau
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Door12 extends FFScriptable {
    /**
     * Creates a new instance of {@link OrcCleaverScript}.
     * @param io the IO associated with the script
     */
    public Door12(final FFInteractiveObject io) {
        super(io);
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onInit()
     */
    @Override
    public int onInit() throws RPGException {
        return super.onInit();
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onInitEnd()
     */
    @Override
    public int onInitEnd() throws RPGException {
        super.getIO().setPosition(new SimpleVector2(648, 1337));
        return super.onInitEnd();
    }
}
