package com.dalonedrow.module.ff.rpg;

import com.dalonedrow.module.ff.constants.FFEquipmentElements;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.IoNpcData;
import com.dalonedrow.rpg.base.flyweights.RPGException;

/**
 * @author drau
 */
public final class FFNpc extends IoNpcData<FFInteractiveObject> {
    /** the list of attributes and their matching names and modifiers. */
    private static final Object[][] attributeMap = new Object[][] {
            { "ST", "Stamina",
                    FFEquipmentElements.valueOf("ELEMENT_STAMINA").getIndex() },
            { "MST", "Max Stamina",
                    FFEquipmentElements.valueOf("ELEMENT_MAX_STAMINA")
                            .getIndex() },
            { "SK", "Skill",
                    FFEquipmentElements.valueOf("ELEMENT_SKILL").getIndex() },
            { "MSK", "Max Skill",
                    FFEquipmentElements.valueOf("ELEMENT_MAX_SKILL")
                            .getIndex() },
            { "DMG", "Damage",
                    FFEquipmentElements.valueOf("ELEMENT_DAMAGE")
                            .getIndex() } };
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
    @Override
    protected void applyRulesModifiers() throws RPGException {
        // TODO Auto-generated method stub

    }
    @Override
    protected void applyRulesPercentModifiers() {
        // TODO Auto-generated method stub

    }
    @Override
    public void ARX_EQUIPMENT_RecreatePlayerMesh() {
        // TODO Auto-generated method stub

    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void ARX_NPC_ManagePoison() {
        // TODO Auto-generated method stub

    }
    @Override
    protected void awardXpForNpcDeath(int xp, FFInteractiveObject killerIO) {
        // NO XP
    }
    @Override
    public boolean calculateBackstab() {
        return false;
    }
    @Override
    public boolean calculateCriticalHit() {
        return false;
    }
    @Override
    protected void damageNonLivingNPC(float dmg, int srcIoid,
            boolean isSpellDamage) throws RPGException {
        // TODO Auto-generated method stub

    }
    @Override
    protected Object[][] getAttributeMap() {
        return attributeMap;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public float getBaseLife() {
        return super.getBaseAttributeScore("ST");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public float getBaseMana() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public float getFullDamage() {
        return super.getFullAttributeScore("DMG");
    }
    @Override
    public float getMaxLife() {
        return super.getBaseAttributeScore("MST");
    }
    @Override
    public int getPoisonned() {
        // TODO Auto-generated method stub
        return 0;
    }
    /**
     * Gets the status strings used in the display.
     * @return {@link String}[]
     * @throws RPGException if an error occurs
     */
    public String[] getStatusString() throws RPGException {
        super.computeFullStats();
        String sk = "", st = "";
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append((int) getFullAttributeScore("SK"));
            sb.append('/');
            sb.append((int) getFullAttributeScore("MSK"));
            sk = sb.toString();
            sb.setLength(0);
            sb.append((int) getFullAttributeScore("ST"));
            sb.append('/');
            sb.append((int) getFullAttributeScore("MST"));
            st = sb.toString();
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb.returnToPool();
        sb = null;
        String[] s = new String[] { new String(super.getTitle()) , sk, st };
        sk = null;
        st = null;
        return s;
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
