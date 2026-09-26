package com.mrcrayfish.guns.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class IncurableEffect extends MobEffect
{
    public IncurableEffect(MobEffectCategory typeIn, int liquidColorIn)
    {
        super(typeIn, liquidColorIn);
    }

    // Fabric port: Forge MobEffect#getCurativeItems does not exist on vanilla; the
    // milk/curative semantics require a narrow hook - TODO(T08) mixin per plan.
    public List<ItemStack> getCurativeItems()
    {
        return Collections.emptyList();
    }


}
