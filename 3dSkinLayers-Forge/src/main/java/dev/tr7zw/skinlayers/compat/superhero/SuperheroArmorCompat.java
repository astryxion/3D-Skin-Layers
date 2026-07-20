package dev.tr7zw.skinlayers.compat.superhero;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Fisk's Superheroes and Legends bring their own player models.
 * While any of their armor is worn, 3D skin layers must step aside so they
 * do not poke through the suit.
 */
@SideOnly(Side.CLIENT)
public final class SuperheroArmorCompat {

    private SuperheroArmorCompat() {
    }

    public static boolean isSuperheroArmorItem(Item item) {
        if (item == null) {
            return false;
        }

        String name = item.getClass().getName();
        if (name.contains("ItemHeroArmor")) {
            return true;
        }
        if (name.contains("ItemLegendsCharacterArmor") || name.contains("ItemLegacySuitArmor")) {
            return true;
        }
        if (name.contains("fiskmods.heroes") && item instanceof ItemArmor) {
            return true;
        }

        return (name.contains("tihyo.legends") || name.contains("tihyo.superheroes")) && item instanceof ItemArmor;
    }

    public static boolean isWearingSuperheroSuit(EntityPlayer player) {
        if (player == null || player.inventory == null) {
            return false;
        }

        for (int i = 0; i < 4; ++i) {
            ItemStack stack = player.inventory.armorItemInSlot(i);
            if (stack != null && isSuperheroArmorItem(stack.getItem())) {
                return true;
            }
        }

        return false;
    }
}
