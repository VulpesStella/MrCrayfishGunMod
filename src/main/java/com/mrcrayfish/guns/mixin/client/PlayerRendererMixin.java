package com.mrcrayfish.guns.mixin.client;

import com.mrcrayfish.guns.client.handler.PlayerModelHandler;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric replacement for RenderPlayerEvent.Pre/Post: the Pre hook applies the held
 * animation's player pre-render transforms, the Post hook restores the model part
 * defaults after rendering.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin
{
    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void cgm$onRenderPlayerPre(AbstractClientPlayer player, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci)
    {
        PlayerModelHandler.get().onRenderPlayerPre(player, partialTick, poseStack, bufferSource);
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void cgm$onRenderPlayerPost(AbstractClientPlayer player, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci)
    {
        PlayerModel<AbstractClientPlayer> model = (PlayerModel<AbstractClientPlayer>) (Object) ((PlayerRenderer) (Object) this).getModel();
        PlayerModelHandler.get().onRenderPlayerPost(model, player.getModelName().equals("slim"));
    }
}
