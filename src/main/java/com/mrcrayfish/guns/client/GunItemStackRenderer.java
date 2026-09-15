package com.mrcrayfish.guns.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class GunItemStackRenderer extends BlockEntityWithoutLevelRenderer
{
    public GunItemStackRenderer()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext display, PoseStack poseStack, MultiBufferSource source, int light, int overlay)
    {
        // Fabric invokes this inside ItemRenderer#render's pushed frame, after
        // applying the display transform and (-0.5, -0.5, -0.5) translation.
        // renderWeapon applies its own display transform, so start at the parent.
        poseStack.popPose();

        poseStack.pushPose();
        try
        {
            Minecraft mc = Minecraft.getInstance();
            if(display == ItemDisplayContext.GROUND)
            {
                GunRenderingHandler.get().applyWeaponScale(stack, poseStack);
            }
            GunRenderingHandler.get().renderWeapon(mc.player, stack, display, poseStack, source, light, mc.getFrameTime());
        }
        finally
        {
            poseStack.popPose();
            // Restore the frame depth expected by ItemRenderer's final pop.
            poseStack.pushPose();
        }
    }
}
