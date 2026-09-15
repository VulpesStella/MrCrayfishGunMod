package com.mrcrayfish.guns.mixin.common;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Author: MrCrayfish
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void modifyApplyKnockbackArgs(LivingEntity instance, double strength, double x, double z, DamageSource source, float amount)
    {
        double modified = strength;
        if(source.getDirectEntity() instanceof ProjectileEntity)
        {
            if(!Config.COMMON.gameplay.enableKnockback.get())
            {
                modified = 0;
            }
            else
            {
                double configStrength = Config.COMMON.gameplay.knockbackStrength.get();
                if(configStrength > 0)
                {
                    modified = configStrength;
                }
            }
        }
        instance.knockback(modified, x, z);
    }
}
