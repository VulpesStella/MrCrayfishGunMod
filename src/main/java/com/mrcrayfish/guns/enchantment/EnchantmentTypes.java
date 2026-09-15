package com.mrcrayfish.guns.enchantment;

import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Fabric port: vanilla 1.20.1 {@link net.minecraft.world.item.enchantment.EnchantmentCategory}
 * is a closed enum and has no Forge-style {@code create}. Categories are expressed as
 * predicates and applied through {@link GunEnchantment#canEnchant}.
 * TODO(T08): verify enchanting table and anvil candidate paths against 1.20.1 sources.
 */
public class EnchantmentTypes
{
    public static final Predicate<ItemStack> GUN = stack -> stack.getItem() instanceof GunItem;
    public static final Predicate<ItemStack> SEMI_AUTO_GUN = stack -> stack.getItem() instanceof GunItem && !((GunItem) stack.getItem()).getGun().getGeneral().isAuto();
}
