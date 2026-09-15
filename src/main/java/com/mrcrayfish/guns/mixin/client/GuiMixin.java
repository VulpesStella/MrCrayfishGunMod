package com.mrcrayfish.guns.mixin.client;

import com.mrcrayfish.guns.client.handler.CrosshairHandler;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric replacement for the canceled RenderGuiOverlayEvent.Pre crosshair overlay:
 * the vanilla crosshair is skipped while a gun is held or while aiming, and the
 * custom crosshair is drawn through HudRenderCallback.
 */
@Mixin(Gui.class)
public abstract class GuiMixin
{
    @Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("HEAD"), cancellable = true)
    private void cgm$onRenderCrosshair(net.minecraft.client.gui.GuiGraphics graphics, CallbackInfo ci)
    {
        if (CrosshairHandler.get().shouldHideVanillaCrosshair())
        {
            ci.cancel();
        }
    }
}
