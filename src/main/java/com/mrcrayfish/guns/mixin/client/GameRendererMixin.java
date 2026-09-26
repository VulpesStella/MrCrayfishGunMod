package com.mrcrayfish.guns.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mrcrayfish.guns.client.handler.AimingHandler;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    /**
     * Fabric replacement for ViewportEvent.ComputeFov: AimingHandler modifies the level
     * FOV (when the configured FOV is used), GunRenderingHandler modifies the first-person
     * hand viewport FOV (when it is not).
     */
    @org.spongepowered.asm.mixin.injection.Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void cgm$onGetFov(net.minecraft.client.Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Double> cir)
    {
        double fov = AimingHandler.get().onFovUpdate((float) cir.getReturnValueD(), useFovSetting);
        fov = GunRenderingHandler.get().onComputeFov(fov, useFovSetting, partialTick);
        cir.setReturnValue(fov);
    }
}
