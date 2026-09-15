package com.mrcrayfish.guns.mixin.common;

import com.google.common.collect.Lists;
import com.mrcrayfish.guns.enchantment.GunEnchantment;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Vanilla 1.20.1 consults {@code enchantment.category.canEnchant(item)} directly in
 * this loop (Forge replaced that call with canApplyAtEnchantingTable), so
 * {@link GunEnchantment#canEnchant} would never run and gun enchantments could never
 * appear at the enchanting table (their placeholder category is WEAPON). The loop is
 * supplemented at RETURN with the predicate check, plus the baseline allowance of
 * BINDING_CURSE on attachment items (was an Item#canApplyAtEnchantingTable override
 * in ScopeItem). The anvil path goes through Enchantment#canEnchant directly and
 * is handled separately by EnchantmentMixin for attachment binding curses.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin
{
    @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"), cancellable = true)
    private static void cgm$getAvailableEnchantmentResults(int cost, ItemStack stack, boolean allowTreasure, CallbackInfoReturnable<List<EnchantmentInstance>> cir)
    {
        if(stack.is(Items.BOOK)) return;
        List<EnchantmentInstance> list = Lists.newArrayList(cir.getReturnValue());
        list.removeIf(entry -> entry.enchantment instanceof GunEnchantment && !entry.enchantment.canEnchant(stack));

        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT)
        {
            boolean applicable;
            if (enchantment instanceof GunEnchantment gunEnchantment)
            {
                applicable = gunEnchantment.canEnchant(stack);
            }
            else if (stack.getItem() instanceof IAttachment && enchantment == net.minecraft.world.item.enchantment.Enchantments.BINDING_CURSE)
            {
                applicable = true;
            }
            else
            {
                continue;
            }

            if ((!enchantment.isTreasureOnly() || allowTreasure) && enchantment.isDiscoverable() && applicable
                    && list.stream().noneMatch(entry -> entry.enchantment == enchantment))
            {
                for (int j = enchantment.getMaxLevel(); j > enchantment.getMinLevel() - 1; j--)
                {
                    if (cost >= enchantment.getMinCost(j) && cost <= enchantment.getMaxCost(j))
                    {
                        list.add(new EnchantmentInstance(enchantment, j));
                        break;
                    }
                }
            }
        }
        cir.setReturnValue(list);
    }
}
