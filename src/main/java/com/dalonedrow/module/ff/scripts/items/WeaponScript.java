package com.dalonedrow.module.ff.scripts.items;

import com.dalonedrow.engine.systems.base.Diceroller;
import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.JOGLErrorHandler;
import com.dalonedrow.module.ff.rpg.Direction;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFRoomData;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.module.ff.systems.FFController;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.constants.Dice;
import com.dalonedrow.rpg.base.constants.ScriptConsts;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.flyweights.ScriptAction;
import com.dalonedrow.rpg.base.systems.Script;

/**
 * 
 * @author drau
 *
 */
@SuppressWarnings("unchecked")
public class WeaponScript extends FFScriptable {
	/**
	 * Creates a new instance of {@link WeaponScript}.
	 * @param io the IO associated with this script
	 */
	public WeaponScript(final FFInteractiveObject io) {
		super(io);
	}
    /**
     * Enchants the item.
     * @throws RPGException if an error occurs
     */
    private void enchant() throws RPGException {
        super.getIO().getItemData().setPrice(
                super.getLocalIntVariableValue("tmp"));
        super.setLocalVariable("tmp", 0);
        super.setLocalVariable("reagent", "none");
    }
    /**
     * Break the item.
     * @return {@link int}
     * @throws PooledException if an error occurs
     * @throws RPGException if an error occurs
     */
    public int onBreak() throws PooledException, RPGException {
        // PLAY "broken_weapon"
        Interactive.getInstance().ARX_INTERACTIVE_DestroyIO(
                super.getIO());
        return ScriptConsts.ACCEPT;
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onCombine()
     */
    @Override
    public int onCombine() throws RPGException {
        // disabled, for now.
        FFInteractiveObject playerIO = ((FFController)
                FFController.getInstance()).getPlayerIO();
        FFInteractiveObject itemIO =
                (FFInteractiveObject) Interactive.getInstance().getIO(
                        super.getLocalIntVariableValue("combined_with"));
        if (itemIO != null) {
            String mixedWith = new String(itemIO.getItemData().getItemName());
            String myName = new String(
                    super.getIO().getItemData().getItemName());
            // if combining with a green potion
            if (mixedWith.equalsIgnoreCase("Green Potion")) {
                if (super.getLocalIntVariableValue("poisonable") == 0) {
                    // send message that the weapon can't be poisoned
                } else {
                    if (playerIO.getPCData().getFullAttributeScore("SK") < 30) {
                        // send message player isn't skilled enough
                    } else {
                        // send event to potion to empty itself
                        Script.getInstance().stackSendIOScriptEvent(
                                itemIO,
                                0,
                                null,
                                "Empty");
                        int tmp = (int)
                                playerIO.getPCData().getFullAttributeScore(
                                        "SK");
                        tmp -= 27; // change temp value to 3 to 73
                        tmp /= 3; // change temo value to 1 to 24
                        super.getIO().setPoisonLevel(tmp);
                        super.getIO().setPoisonCharges(tmp);
                        // send message to player that weapon is poisoned
                    }
                }
            } else if (mixedWith.equalsIgnoreCase("egg")) {
                // send message that you cannot combine
            } else if (super.getLocalIntVariableValue("enchanted") == 1) {
                // send message that you cannot combine
            } else if (mixedWith.equalsIgnoreCase("dragon egg")) {
                if ("Meteor Sabre".equalsIgnoreCase(myName)
                        || "Meteor Zweihander".equalsIgnoreCase(myName)) {
                    super.setLocalVariable("reagent", "egg");
                    // show halo graphic
                    try {
                        reagentMixed();
                    } catch (PooledException e) {
                        throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
                    }
                } else {
                    // send message to player that you can't combine
                }
            } else if ("Meteor Sabre".equalsIgnoreCase(myName)
                    || "Meteor Zweihander".equalsIgnoreCase(myName)) {
                // send message to player that you can't combine
            } else if (mixedWith.equalsIgnoreCase("garlic")) {
                super.setLocalVariable("reagent", "garlic");
                // show halo graphic
                try {
                    reagentMixed();
                } catch (PooledException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
                }
            } else if (mixedWith.equalsIgnoreCase("bone powder")
                    || mixedWith.equalsIgnoreCase("dragon bone powder")
                    || mixedWith.equalsIgnoreCase("golem heart")
                    || mixedWith.equalsIgnoreCase("amikar rock")) {
                super.setLocalVariable("reagent", mixedWith);
                // show halo graphic
                try {
                    reagentMixed();
                } catch (PooledException e) {
                    throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
                }
            }
        }
        return super.onCombine();
    }
    /** no durability in this game. */
    public int onDurability() {
        // SET_DURABILITY -c ~^$PARAM1~
        return ScriptConsts.ACCEPT;
    }
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onEquip()
     */
    @Override
    public int onEquip() throws RPGException {
        // play sound file "equip_sword"
        // PLAY "equip_sword"
        return super.onEquip();
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
    /*
     * (non-Javadoc)
     * @see com.dalonedrow.rpg.base.flyweights.Scriptable#onInventoryUse()
     */
    @Override
    public int onInventoryUse() throws RPGException {
    	int fighting = Script.getInstance().getGlobalIntVariableValue(
    			"FIGHTING");
    	if (fighting == 0) {
    		// player isn't fighting already
    		// check to see if player is strong enough to use?
    		// if player isn't strong enough to wield
    		// send a message
    		// else if player isn't skilled enough to wield
    		// send a message
    		// else
    		// have player equip the item
    		super.getIO().getItemData().ARX_EQUIPMENT_Equip(
    		        ((FFController) FFController.getInstance()).getPlayerIO());
    	}
    	return super.onInventoryUse();
    }
    /** No repair in this game. */
    public int onRepaired() {
    	// if (super.getLocalIntVariableValue("repair_check_durability") == 1) {
    	// IF (^DURABILITY == ^MAXDURABILITY) {
    	// SPEAK -p [player_weapon_already_repaired] NOP
    	// ACCEPT
    	// }
    	// SENDEVENT REPAIR ^SENDER ""
    	// ACCEPT
    	// }
    	// SET §tmp ~^MAXDURABILITY~
    	// REPAIR SELF ~^PLAYER_SKILL_OBJECT_KNOWLEDGE~
    	// if (^DURABILITY < §tmp) {
    	// SPEAK -p [player_weapon_repaired_partially] NOP
    	// ACCEPT
    	// }
    	// SPEAK -p [player_weapon_repaired_in_full] NOP
    	// UNSET §tmp
    	// ACCEPT
    	return ScriptConsts.ACCEPT;
    }
    public int onSpellCast() throws RPGException {
    	if ("ENCHANT WEAPON".equalsIgnoreCase(
    			super.getLocalStringVariableValue("spell_cast"))) {
    		if (super.getLocalIntVariableValue("enchanted") == 1) {
    			// send message cannot enchant
    			// SPEAK -p [player_no] NOP
    		} else {
    			if ("none".equalsIgnoreCase(
    					super.getLocalStringVariableValue("reagent"))) {
    				// send message cannot enchant
    				// SPEAK -p [player_wrong] NOP
    			} else {
    				// play spell sound
    				// PLAY -o "Magic_Spell_Enchant_Weapon"
    				// enchanting with dragon egg
    				if ("egg".equalsIgnoreCase(
    						super.getLocalStringVariableValue("reagent"))) {
    					if (super.getLocalIntVariableValue(
    							"caster_skill_level") < 8) {
    						// send message not skilled enough
    						// SPEAK -p [player_not_skilled_enough] NOP
    					} else {
    						super.setLocalVariable("enchanted", 1);
    						if (Script.getInstance().getGlobalIntVariableValue(
    								"need_superweapon") == 1) {
    							// update quest book
    							// QUEST [system_Quest_log_final_meeting]
    							// HEROSAY [system_questbook_updated]
    							// play sound for system alerts
    							// PLAY SYSTEM
    						}
    						if (Script.getInstance().getGlobalIntVariableValue(
    								"superweapon") < 2) {
    							Script.getInstance().setGlobalVariable(
    									"weapon_enchanted", 2);
    							Script.getInstance().setGlobalVariable(
    									"need_superweapon", 2);
    							Script.getInstance().setGlobalVariable(
    									"superweapon", 2);
    						}
    						super.setLocalVariable("reagent", "none");
    						String myName = new String(
    								super.getIO().getItemData().getItemName());
    						if ("Meteor Sabre".equalsIgnoreCase(myName)) {
    							// replace me with an enchanted Meteor Sabre
    							// REPLACEME "SABRE_METEOR_ENCHANT"
    						} else if ("Meteor Zweihander"
    								.equalsIgnoreCase(myName)) {
    							// replace me with an enchanted Meteor Sabre
    							// REPLACEME "SWORD_2HANDED_METEOR_ENCHANT"
    						}
    					}
    				} else {
    					super.setLocalVariable("enchanted", 1);
    					super.getIO().getItemData().setItemName("Axe");
    					// SETNAME [description_axe]
    					if ("garlic".equalsIgnoreCase(
    							super.getLocalStringVariableValue("reagent"))) {
    						// play halo effect
    						// if player casting skill < 50,
    						// set affect DEX by 1, else by 2
    						// HALO -ocs 0 1 0 30
    						// IF (^PLAYER_SKILL_CASTING < 50) {
    						// SETEQUIP DEXTERITY 1
    						// }
    						// IF (^PLAYER_SKILL_CASTING > 50) {
    						// SETEQUIP DEXTERITY 2
    						// }
    						float tmp = super.getIO().getItemData().getPrice();
    						tmp *= 1.5f;
    						super.setLocalVariable("tmp", tmp);
    						enchant();
    					} else if ("bone powder".equalsIgnoreCase(
    							super.getLocalStringVariableValue("reagent"))) {
    						// play halo effect
    						// if player casting skill < 50,
    						// set affect STR by 2, else by 3
    						// HALO -ocs 1 0.5 0 30
    						// IF (^PLAYER_SKILL_CASTING < 50) {
    						// SETEQUIP STRENGTH 2
    						// }
    						// IF (^PLAYER_SKILL_CASTING > 50) {
    						// SETEQUIP STRENGTH 3
    						// }
    					    float tmp = super.getIO().getItemData().getPrice();
    						tmp *= 3f;
    						super.setLocalVariable("tmp", tmp);
    						enchant();
    					} else if ("dragon bone powder".equalsIgnoreCase(
    							super.getLocalStringVariableValue("reagent"))) {
    						// play halo effect
    						// set affect STR by 1
    						// HALO -ocs 1 0 0 30
    						// SETEQUIP STRENGTH 1
    					    float tmp = super.getIO().getItemData().getPrice();
    						tmp *= 1.5f;
    						super.setLocalVariable("tmp", tmp);
    						enchant();
    					}
    				}
    			}
    		}
    	}
    	return ScriptConsts.ACCEPT;
    }
    private void reagentMixed() throws PooledException, RPGException {
    	FFInteractiveObject itemIO =
    			(FFInteractiveObject) Interactive.getInstance().getIO(
    					super.getLocalIntVariableValue("combined_with"));
    	Interactive.getInstance().ARX_INTERACTIVE_DestroyIO(itemIO);
    	// kill the haloe
    	// TIMERoff 1 1 HALO -f
    	// SPEAK -p [Player_off_interesting] NOP
    }
}
