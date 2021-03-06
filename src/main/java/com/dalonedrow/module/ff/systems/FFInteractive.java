package com.dalonedrow.module.ff.systems;

import com.dalonedrow.engine.sprite.base.SimplePoint;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.ProjectConstants;
import com.dalonedrow.module.ff.rpg.FFCharacter;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.scripts.pc.Hero;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.Script;
import com.dalonedrow.utils.ArrayUtilities;

/**
 * @author 588648
 */
@SuppressWarnings("unchecked")
public class FFInteractive extends Interactive<FFInteractiveObject> {
    /** the next available id. */
    private int nextId;
    /** the list of {@link FFInteractiveObject}s. */
    private FFInteractiveObject[] objs;
    /** Creates a new instance of {@link FFInteractive}. */
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
    /**
     * Gets the IO that occupies a specific position.  No two IOs can occupy
     * the same position.
     * @param pt the {@link SimplePoint}
     * @return {@link FFInteractiveObject}
     */
    public FFInteractiveObject getIoAtPosition(final SimplePoint pt) {
        FFInteractiveObject io = null;
        for (int i = objs.length - 1; i >= 0; i--) {
            FFInteractiveObject ioo = objs[i];
            if (ioo != null
                    && ioo.getPosition() != null
                    && ioo.getPosition().equals(pt)) {
                io = ioo;
            }
        }
        return io;
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
        // io.setScript(new MasterScript());
        // io.addIOFlag(FFIo.IO_16_IMMORTAL);
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
     * Gets an NPC object by its name.
     * @param name the name
     * @return {@link FFInteractiveObject}
     */
    public FFInteractiveObject getNpcByName(final String name) {
        FFInteractiveObject io = null;
        for (int i = objs.length - 1; i >= 0; i--) {
            if (objs[i] != null
                    && objs[i].hasIOFlag(IoGlobals.IO_03_NPC)
                    && name.equalsIgnoreCase(
                            new String(objs[i].getNPCData().getName()))) {
                io = objs[i];
                break;
            }
        }
        return io;
    }
    /**
     * Gets a new Player IO.
     * @return {@link FFInteractiveObject}
     * @throws RPGException
     */
    public final FFInteractiveObject newHero()
            throws RPGException {
        FFInteractiveObject io = getNewIO();
        io.addIOFlag(IoGlobals.IO_01_PC);
        io.setPCData(new FFCharacter());
        io.getPCData().newHero();
        ((FFController) ProjectConstants.getInstance())
                .setPlayer(io.getRefId());
        io.setScript(new Hero(io));
        Script.getInstance().sendInitScriptEvent(io);
        return io;
    }
    /**
     * Gets a new Item IO
     * @return {@link FFInteractiveObject}
     * @throws RPGException
     */
    public final FFInteractiveObject newItem()
            throws RPGException {
        FFInteractiveObject io = getNewIO();
        io.addIOFlag(IoGlobals.IO_02_ITEM);
        return io;
    }
    /**
     * Gets a new Item IO
     * @return {@link FFInteractiveObject}
     * @throws RPGException
     */
    public final FFInteractiveObject newNPC()
            throws RPGException {
        FFInteractiveObject io = getNewIO();
        io.addIOFlag(IoGlobals.IO_03_NPC);
        return io;
    }
}
