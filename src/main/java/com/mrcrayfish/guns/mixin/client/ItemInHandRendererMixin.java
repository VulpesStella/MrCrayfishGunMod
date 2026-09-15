package com.mrcrayfish.guns.mixin.client;

import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.client.handler.RecoilHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric replacement for the Forge RenderHandEvent, which fired per hand before
 * vanilla first-person hand rendering. Hooking renderArmWithItem gives the same
 * per-hand data (item, equip progress, swing progress, partial tick).
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin
{
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void cgm$onRenderArmWithItem(AbstractClientPlayer player, float partialTick, float attackAnim, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci)
    {
        RecoilHandler.get().onRenderHandCooldown(stack, Minecraft.getInstance().getFrameTime());
        if (GunRenderingHandler.get().onRenderHand(poseStack, hand, stack, partialTick, bufferSource, packedLight))
        {
            ci.cancel();
        }
    }
}
