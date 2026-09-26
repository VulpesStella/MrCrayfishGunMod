package com.mrcrayfish.guns.mixin.common;

import com.mrcrayfish.guns.effect.IncurableEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MilkBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Preserve the milk-curing rule without changing commands or other effect removal. */
@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin
{
    @Redirect(method = "finishUsingItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean cgm$cureEffects(LivingEntity entity)
    {
        if(entity.level().isClientSide)
            return false;
        boolean changed = false;
        for(MobEffect effect : entity.getActiveEffects().stream().map(MobEffectInstance::getEffect).toList())
        {
            if(!(effect instanceof IncurableEffect))
                changed |= entity.removeEffect(effect);
        }
        return changed;
    }
}
