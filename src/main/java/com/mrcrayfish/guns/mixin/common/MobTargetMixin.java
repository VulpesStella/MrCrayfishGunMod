package com.mrcrayfish.guns.mixin.common;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.init.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port of the Forge LivingChangeTargetEvent handler in
 * ThrowableStunGrenadeEntity#blindMobs: blinded mobs must not acquire a new
 * target. The baseline handler only reacted when the mob already had a target
 * (originalTarget != null), which is mirrored here by checking getTarget()
 * before the change. Clearing the target (null) stays allowed.
 */
@Mixin(Mob.class)
public abstract class MobTargetMixin
{
    @Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void cgm$blindedTargetChange(LivingEntity target, CallbackInfo ci)
    {
        Mob self = (Mob) (Object) this;
        if (Config.COMMON.stunGrenades.blind.blindMobs.get() && self.hasEffect(ModEffects.BLINDED.get()) && self.getTarget() != null)
        {
            ci.cancel();
        }
    }
}
