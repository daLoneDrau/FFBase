package com.dalonedrow.module.ff.rpg;

import com.dalonedrow.module.ff.constants.FFEquipmentElements;
import com.dalonedrow.module.ff.net.FFWebServiceClient;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.constants.Dice;
import com.dalonedrow.rpg.base.flyweights.IOEquipItem;
import com.dalonedrow.rpg.base.flyweights.IoPcData;
import com.dalonedrow.rpg.base.flyweights.RPGException;

/**
 * @author drau
 */
@SuppressWarnings("unchecked")
public final class FFCharacter extends IoPcData<FFInteractiveObject> {
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
            { "LK", "Luck",
                    FFEquipmentElements.valueOf("ELEMENT_LUCK").getIndex() },
            { "MLK", "Max Luck",
                    FFEquipmentElements.valueOf("ELEMENT_MAX_LUCK")
                            .getIndex() },
            { "DMG", "Damage",
                    FFEquipmentElements.valueOf("ELEMENT_DAMAGE")
                            .getIndex() } };
    /** flag indicating pretty printing has been turned on. */
    private boolean prettyPrinting;
    private boolean REFUSE_GAME_RETURN;
    /**
     * Creates a new instance of {@link FFCharacter}.
     * @throws RPGException if an error occurs
     */
    public FFCharacter() throws RPGException {
        super();
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
    /**
     * {@inheritDoc}
     */
    @Override
    public void ARX_EQUIPMENT_RecreatePlayerMesh() {
        // TODO Auto-generated method stub

    }
    @Override
    public boolean calculateBackstab() {
        return false;
    }
    @Override
    public boolean calculateCriticalHit() {
        return false;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canIdentifyEquipment(final IOEquipItem equipitem) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    protected Object[][] getAttributeMap() {
        return FFCharacter.attributeMap;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected float getBaseLife() {
        return super.getFullAttributeScore("ST");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected float getBaseMana() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public float getFullDamage() {
        return super.getFullAttributeScore("DMG");
    }
    @Override
    protected String getLifeAttribute() {
        return "ST";
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInCombat() {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public float getMaxLife() {
        return super.getBaseAttributeScore("MST");
    }
    public void newHero() throws RPGException {
        // roll stats
        int roll = Dice.ONE_D6.roll() + 6;
        super.setBaseAttributeScore("SK", roll);
        super.setBaseAttributeScore("MSK", roll);
        roll = Dice.TWO_D6.roll() + 12;
        super.setBaseAttributeScore("ST", roll);
        super.setBaseAttributeScore("MST", roll);
        roll = Dice.ONE_D6.roll() + 6;
        super.setBaseAttributeScore("LK", roll);
        super.setBaseAttributeScore("MLK", roll);
        // equip iron sword
        FFWebServiceClient.getInstance().loadItem(
                "IRON SWORD").getItemData().ARX_EQUIPMENT_Equip(getIo());
        super.computeFullStats();
    }
    /**
     * Sets the value of the flag indicating pretty printing has been turned on.
     * @param flag the new value to set
     */
    public void setPrettyPrinting(final boolean flag) {
        prettyPrinting = flag;
    }
    /**
     * Tests the player's luck.
     * @param isCombat if <tt>true</tt>, this is tested during combat, and the
     *            player's Luck score will be lowered by 1, no matter the
     *            outcome
     * @return if <tt>true</tt> the player passes the luck test; otherwise they
     *         fail
     * @throws RPGException if an error occurs
     */
    public boolean testYourLuck(final boolean isCombat) throws RPGException {
        boolean passed = false;
        super.computeFullStats();
        // roll 2 dice. if that is less than equal to Luck, the test passed
        int roll = Dice.TWO_D6.roll();
        int score = (int) super.getFullAttributeScore("LK");
        if (false) {
            // if (roll <= score) {
            passed = true;
        }
        if (isCombat) {
            // remove one luck point
            super.setBaseAttributeScore(
                    "LK", super.getBaseAttributeScore("LK") - 1);
        }
        return passed;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String s = null;
        try {
            PooledStringBuilder sb =
                    StringBuilderPool.getInstance().getStringBuilder();
            if (prettyPrinting) {
                sb.append(new String(super.getName()));
                sb.append('\n');
                sb.append("SK: ");
                sb.append((int) super.getFullAttributeScore("SK"));
                sb.append('/');
                sb.append((int) super.getFullAttributeScore("MSK"));
                sb.append(System.lineSeparator());
                sb.append("ST: ");
                sb.append((int) super.getFullAttributeScore("ST"));
                sb.append('/');
                sb.append((int) super.getFullAttributeScore("MST"));
                sb.append(System.lineSeparator());
                sb.append("LK: ");
                sb.append((int) super.getFullAttributeScore("LK"));
                sb.append('/');
                sb.append((int) super.getFullAttributeScore("MLK"));
                sb.append(System.lineSeparator());
                prettyPrinting = false;
            } else {}
            s = sb.toString();
            sb.returnToPool();
            sb = null;
        } catch (PooledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;
    }
}
