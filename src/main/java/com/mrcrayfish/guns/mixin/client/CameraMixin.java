package com.mrcrayfish.guns.mixin.client;

import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric replacement for ViewportEvent.ComputeCameraAngles roll support. Vanilla
 * 1.20.1 Camera has no roll; the roll is applied to the rotation quaternion after
 * setup. Directional vectors are left un-rolled (cosmetic effect only).
 */
@Mixin(Camera.class)
public abstract class CameraMixin
{
    @Inject(method = "setup", at = @At("TAIL"))
    private void cgm$onCameraSetup(net.minecraft.world.level.BlockGetter level, net.minecraft.world.entity.Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci)
    {
        float roll = GunRenderingHandler.get().onCameraSetupRoll(partialTick);
        if (roll != 0.0F)
        {
            ((Camera) (Object) this).rotation().rotateZ((float) Math.toRadians(roll));
        }
    }
}
