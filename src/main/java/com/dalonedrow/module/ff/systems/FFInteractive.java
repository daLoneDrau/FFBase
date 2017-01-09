package com.dalonedrow.module.ff.systems;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.module.ff.rpg.FFCharacter;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFItem;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.Scriptable;
import com.dalonedrow.rpg.base.systems.Script;
import com.dalonedrow.utils.ArrayUtilities;

/**
 * 
 * @author 588648
 *
 */
@SuppressWarnings("unchecked")
public class FFInteractive extends Interactive<FFInteractiveObject> {
    /** the next available id. */
    private int nextId;
    /** the list of {@link FFInteractiveObject}s. */
    private FFInteractiveObject[] objs;

    public FFInteractive() {
        super.setInstance(this);
        objs = new FFInteractiveObject[0];
    }

    @Override
    public void addAnimation(int id, int animId) throws RPGException {
        // TODO Auto-generated method stub

    }

    @Override
    public FFInteractiveObject addItem(String item, long flags)
            throws RPGException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void ARX_INTERACTIVE_ForceIOLeaveZone(FFInteractiveObject io,
            long flags) {
        // TODO Auto-generated method stub

    }
    @Override
    protected FFInteractiveObject[] getIOs() {
        return objs;
    }
    /**
     * Gets the master script object.
     * @return {@link FFInteractiveObject}
     */
    public FFInteractiveObject getMasterScript() {
        FFInteractiveObject io = getNewIO();
        // TODO - set master script
        //io.setScript(new MasterScript());
        //io.addIOFlag(FFIo.IO_16_IMMORTAL);
        return io;
    }
    @Override
    public int getMaxIORefId() {
        return nextId;
    }
    @Override
    protected FFInteractiveObject getNewIO() {
        // step 1 - find the next id
        int id = nextId++;
        FFInteractiveObject io = null;
        // try {
        io = new FFInteractiveObject(id);
        // } catch (RPGException e) {
        // JOGLErrorHandler.getInstance().fatalError(e);
        // }
        // step 2 - find the next available index in the objs array
        int index = -1;
        for (int i = objs.length - 1; i >= 0; i--) {
            if (objs[i] == null) {
                index = i;
                break;
            }
        }
        // step 3 - put the new object into the arrays
        if (index < 0) {
            objs = ArrayUtilities.getInstance().extendArray(io, objs);
        } else {
            objs[index] = io;
        }
        return io;
    }
    /**
     * Gets a new Player IO
     * @return {@link BDDIO}
     * @throws RPGException
     */
    public final FFInteractiveObject newHero()
            throws RPGException {
        FFInteractiveObject io = getNewIO();
        io.addIOFlag(IoGlobals.IO_01_PC);
        io.setPCData(new FFCharacter());
        io.getPCData().newHero();
        return io;
    }
    /**
     * Gets a new Player IO
     * @return {@link BDDIO}
     * @throws RPGException
     */
    public final FFInteractiveObject newItem()
            throws RPGException {
        FFInteractiveObject io = getNewIO();
        io.addIOFlag(IoGlobals.IO_02_ITEM);
        return io;
    }
}
