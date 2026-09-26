package com.mrcrayfish.guns.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public abstract class GunEnchantment extends Enchantment
{
    private final Predicate<ItemStack> categoryPredicate;
    private Type type;

    protected GunEnchantment(Rarity rarityIn, Predicate<ItemStack> typeIn, EquipmentSlot[] slots, Type type)
    {
        // WEAPON is a placeholder; the enum category is never consulted because
        // canEnchant below is the sole applicability check on Fabric.
        super(rarityIn, EnchantmentCategory.WEAPON, slots);
        this.categoryPredicate = typeIn;
        this.type = type;
    }

    @Override
    public boolean canEnchant(ItemStack stack)
    {
        return this.categoryPredicate.test(stack);
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment)
    {
        if(enchantment instanceof GunEnchantment)
        {
            return ((GunEnchantment) enchantment).type != this.type;
        }
        return super.checkCompatibility(enchantment);
    }

    public enum Type
    {
        WEAPON, AMMO, PROJECTILE
    }
}
