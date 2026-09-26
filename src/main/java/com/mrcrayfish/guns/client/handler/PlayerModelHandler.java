package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.GunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric port note: Forge received player render hooks through
 * {@code PlayerModelEvent.Render.Post} and {@code RenderPlayerEvent.Pre/Post}, which have no
 * Fabric 1.20.1 equivalent. The (formerly {@code @SubscribeEvent}) handlers are kept below as
 * plain methods with their logic intact, to be invoked from a player model/player renderer
 * mixin.
 *
 * TODO(T06): wire via PlayerModelMixin (baseline already has mixin/client/PlayerModelMixin).
 *
 * Method signature list (what a mixin should call and when):
 * <ul>
 *     <li>{@code void onRenderPlayerPre(Player player, float partialTick, PoseStack poseStack,
 *     MultiBufferSource bufferSource)} - was {@code RenderPlayerEvent.Pre} (per player, before
 *     the model is set up); applies the held animation's pre-render transforms.</li>
 *     <li>{@code void onRenderPlayerPost(PlayerModel<AbstractClientPlayer> model, boolean slim)}
 *     - was {@code RenderPlayerEvent.Post}; resets the model arm positions to their defaults.</li>
 * </ul>
 *
 * Author: MrCrayfish
 */
public class PlayerModelHandler
{
    private static final PlayerModelHandler INSTANCE = new PlayerModelHandler();

    public static PlayerModelHandler get()
    {
        return INSTANCE;
    }

    /*@SubscribeEvent
    public void onRenderPlayer(PlayerModelEvent.Render.Post event)
    {
        PoseStack poseStack = event.getPoseStack();
        Player player = event.getPlayer();
        ItemStack heldItem = player.getOffhandItem();
        if(!heldItem.isEmpty() && heldItem.getItem() instanceof GunItem)
        {
            poseStack.pushPose();
            Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            if(gun.getGeneral().getGripType().getHeldAnimation().applyOffhandTransforms(player, event.getPlayerModel(), heldItem, poseStack, event.getDeltaTicks()))
            {
                MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                GunRenderingHandler.get().renderWeapon(player, heldItem, ItemTransforms.TransformType.FIXED, poseStack, buffer, event.getLight(), event.getDeltaTicks());
            }
            poseStack.popPose();
        }
    }*/

    /**
     * Forge port note: was {@code @SubscribeEvent RenderPlayerEvent.Pre}.
     *
     * @param player the player being rendered
     * @param partialTick the current partial tick (was event.getPartialTick())
     * @param poseStack the pose stack of the player renderer (was event.getPoseStack())
     * @param bufferSource the buffer source of the player renderer (was event.getMultiBufferSource())
     */
    public void onRenderPlayerPre(Player player, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource)
    {
        ItemStack heldItem = player.getMainHandItem();
        if(!heldItem.isEmpty() && heldItem.getItem() instanceof GunItem)
        {
            Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            gun.getGeneral().getGripType().getHeldAnimation().applyPlayerPreRender(player, InteractionHand.MAIN_HAND, AimingHandler.get().getAimProgress(player, partialTick), poseStack, bufferSource);
        }
    }

    /**
     * Forge port note: was {@code @SubscribeEvent RenderPlayerEvent.Post}.
     *
     * @param model the player model (was event.getRenderer().getModel())
     * @param slim whether the rendered player uses the slim model (was derived from
     *             ((AbstractClientPlayer) event.getEntity()).getModelName())
     */
    public void onRenderPlayerPost(PlayerModel<AbstractClientPlayer> model, boolean slim)
    {
        /* Makes sure the model part positions reset back to original definitions */
        model.rightArm.x = -5.0F;
        model.rightArm.y = slim ? 2.5F : 2.0F;
        model.rightArm.z = 0.0F;
        model.leftArm.x = 5.0F;
        model.leftArm.y = slim ? 2.5F : 2.0F;
        model.leftArm.z = 0.0F;
        /*model.head.x = 5.0F;
        model.leftArm.y = slim ? 2.5F : 2.0F;
        model.leftArm.z = 0.0F;*/
    }
}
