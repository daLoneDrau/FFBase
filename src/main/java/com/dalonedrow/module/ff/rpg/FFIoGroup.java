package com.dalonedrow.module.ff.rpg;

import java.util.HashMap;
import java.util.Iterator;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.module.ff.constants.UiSettings;
import com.dalonedrow.module.ff.systems.FFInteractive;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.constants.IoGlobals;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.utils.ArrayUtilities;

/**
 * @author drau
 */
public final class FFIoGroup extends FFInteractiveObject {
    /** the group's hex location. */
    private int location;
    /** the list of group members. */
    private int[] members;
    /**
     * Creates a new instance of {@link FFIoGroup}.
     * @param id the reference id
     */
    public FFIoGroup(final int id) {
        super(id);
        members = new int[0];
        location = -1;
    }
    /**
     * Adds a member to the group.
     * @param id the new member's reference id
     * @throws RPGException if the reference id is invalid or points to this
     *             group
     */
    public void addMember(final int id) throws RPGException {
        if (!Interactive.getInstance().hasIO(id)) {
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "IOid " + id + " does not exist");
        }
        FFInteractiveObject io =
                (FFInteractiveObject) Interactive.getInstance().getIO(id);
        if (io.hasIOFlag(IoGlobals.IO_14_PARTY)) {
            io = null;
            throw new RPGException(ErrorMessage.INTERNAL_BAD_ARGUMENT,
                    "IOGroup cannot have group members");
        }
        io = null;
        if (!hasMember(id)) {
            members = ArrayUtilities.getInstance().extendArray(
                    id, members);
        }
    }
    /**
     * Finds a group member with a specific reference id.
     * @param id the id
     * @return {@link int}
     */
    private int findMember(final int id) {
        int index = -1;
        for (int i = members.length - 1; i >= 0; i--) {
            if (members[i] == id) {
                index = i;
                break;
            }
        }
        return index;
    }
    /**
     * Gets the group's hex location.
     * @return {@link int}
     */
    public int getLocation() {
        return location;
    }
    /**
     * Gets a member's reference id by its list index.
     * @param index the index
     * @return {@link int}
     */
    public int getMember(final int index) {
        return members[index];
    }
    public String getName() throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        String n = "";
        int pio = ((FFInteractive) Interactive.getInstance()).getPlayerRefId();
        try {
            if (hasMember(pio)) {
                FFInteractiveObject io =
                        (FFInteractiveObject) Interactive.getInstance()
                                .getIO(pio);
                sb.append(io.getPCData().getName());
                sb.append("'s Army");
            } else if (members.length > 0) {
                FFInteractiveObject io =
                        (FFInteractiveObject) Interactive.getInstance()
                                .getIO(members[0]);
                sb.append(size());
                sb.append(' ');
                String name = new String(io.getNPCData().getName());
                sb.append(name);
                if (size() > 1) {
                    if (name.endsWith("x")
                            || name.endsWith("z")) {
                        sb.append("en");
                    } else if (!name.endsWith("s")) {
                        sb.append('s');
                    }
                }
            }
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        n = sb.toString();
        sb.returnToPool();
        sb = null;
        return n;
    }
    /**
     * Counts the number of horses in the group.
     * @return {@link int}
     * @throws RPGException if an error occurs
     */
    public int getNumberOfHorses() throws RPGException {
        int count = 0;
        for (int i = 0, len = members.length; i < len; i++) {
            FFInteractiveObject io = (FFInteractiveObject) Interactive
                    .getInstance().getIO(members[i]);
            if (io.hasIOFlag(IoGlobals.IO_04_HORSE)) {
                count++;
            }
            io = null;
        }
        return count;
    }
    public int getNumberOfMembers() throws RPGException {
        return members.length;
    }
    public String getPartyList() throws RPGException {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0, len = members.length; i < len; i++) {
            FFInteractiveObject io =
                    (FFInteractiveObject) Interactive.getInstance()
                            .getIO(members[i]);
            if (io.hasIOFlag(IoGlobals.IO_04_HORSE)) {
                io = null;
                continue;
            }
            String name = "";
            if (io.hasIOFlag(IoGlobals.IO_01_PC)) {
                name = new String(io.getPCData().getName());
            }
            if (io.hasIOFlag(IoGlobals.IO_03_NPC)) {
                name = new String(io.getNPCData().getName());
            }
            if (map.containsKey(name)) {
                int count = map.get(name);
                count++;
                map.put(name, count);
            } else {
                map.put(name, 1);
            }
            io = null;
            name = null;
        }
        Iterator<String> iter = map.keySet().iterator();
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        while (iter.hasNext()) {
            String n = iter.next();
            int c = map.get(n);
            try {
                if (c > 1) {
                    sb.append(c);
                    sb.append(' ');
                    if (n.endsWith("man")) {
                        sb.append(n.substring(0, n.length() - "man".length()));
                        sb.append("men");
                    } else if (n.endsWith("f")
                            && !n.endsWith("Chief")
                            && !n.endsWith("chief")) {
                        sb.append(n.substring(0, n.length() - "f".length()));
                        sb.append("ves");
                    } else if (n.endsWith("y")) {
                        sb.append(n.substring(0, n.length() - "y".length()));
                        sb.append("ies");
                    } else if (n.endsWith("hild")) {
                        sb.append(n.substring(0, n.length() - "hild".length()));
                        sb.append("hildren");
                    } else if (n.endsWith("ch")) {
                        sb.append(n.substring(0, n.length() - "ch".length()));
                        sb.append("ches");
                    } else if (n.endsWith("ess")) {
                        sb.append(n.substring(0, n.length() - "ess".length()));
                        sb.append("esses");
                    } else if (n.endsWith("s")) {
                        sb.append(n);
                    } else {
                        sb.append(n);
                        sb.append('s');
                    }
                } else {
                    sb.append(n);
                }
                if (iter.hasNext()) {
                    sb.append(System.lineSeparator());
                }
            } catch (PooledException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String s = sb.toString();
        sb.returnToPool();
        sb = null;
        return s;
    }
    public String getUiTablePartyList() throws RPGException {
        String[] split = getPartyList().split("\n");
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        for (int i = 0, len = split.length; i < len; i++) {
            try {
                sb.append(split[i]);
                int j = UiSettings.RIGHT_COLUMN_INNER_WIDTH
                        - split[i].length();
                for (; j > 0; j--) {
                    sb.append(' ');
                }
                sb.append('\n');
            } catch (PooledException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
        }
        String s = sb.toString();
        sb.returnToPool();
        sb = null;
        return s;
    }
    /**
     * Determines if the group has a member with a specific reference id.
     * @param id the id
     * @return <tt>true</tt> if group has that reference id; <tt>false</tt>
     *         otherwise
     * @throws RPGException
     */
    public boolean hasMember(final int id) throws RPGException {
        boolean has = false;
        for (int i = members.length - 1; i >= 0; i--) {
            if (members[i] == id) {
                has = true;
                break;
            }
        }
        return has;
    }
    /**
     * Removed a member from the group.
     * @param refId the member's reference id
     * @throws RPGException
     */
    public void removeMember(final int refId) throws RPGException {
        int index = -1;
        for (int i = members.length - 1; i >= 0; i--) {
            if (members[i] == refId) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            members = ArrayUtilities.getInstance().removeIndex(index, members);
        }
    }
    /**
     * Sets the group's hex location.
     * @param val the new value to set
     */
    public void setLocation(final int val) {
        location = val;
    }
    /**
     * Gets the group's size.
     * @return {@link int}
     */
    public int size() {
        return members.length;
    }
}
