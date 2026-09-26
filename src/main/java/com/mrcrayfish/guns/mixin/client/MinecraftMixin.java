package com.mrcrayfish.guns.mixin.client;

import com.mrcrayfish.guns.client.handler.ShootingHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric replacement for the Forge InputEvent.InteractionKeyMappingTriggered handling
 * (the baseline hooked Forge-patched locals in startUseItem, which do not exist in
 * vanilla). While a gun would consume the click, vanilla attack/use behavior is
 * canceled so the gun's own firing/aiming path handles the input.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void cgm$onStartAttack(CallbackInfoReturnable<Boolean> cir)
    {
        if (ShootingHandler.get().onAttackClick())
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void cgm$onContinueAttack(boolean leftClick, CallbackInfo ci)
    {
        if (net.minecraft.client.Minecraft.getInstance().player != null
                && net.minecraft.client.Minecraft.getInstance().player.getMainHandItem().getItem() instanceof com.mrcrayfish.guns.item.GunItem)
        {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void cgm$onStartUseItem(CallbackInfo ci)
    {
        if (ShootingHandler.get().onUseClick())
        {
            ci.cancel();
        }
    }
}
