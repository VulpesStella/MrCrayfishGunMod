package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

/**
 * Author: MrCrayfish
 */
public class RecoilHandler
{
    private static RecoilHandler instance;

    public static RecoilHandler get()
    {
        if(instance == null)
        {
            instance = new RecoilHandler();
        }
        return instance;
    }

    private Random random = new Random();
    private double gunRecoilNormal;
    private double gunRecoilAngle;
    private float gunRecoilRandom;
    private float cameraRecoil;
    private float progressCameraRecoil;

    private RecoilHandler() {}

    public void onGunFire(GunFireEvent.Post event)
    {
        if(!event.isClient())
            return;

        if(!Config.SERVER.enableCameraRecoil.get())
            return;

        ItemStack heldItem = event.getStack();
        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        float recoilModifier = 1.0F - GunModifierHelper.getRecoilModifier(heldItem);
        recoilModifier *= this.getAdsRecoilReduction(modifiedGun);
        this.cameraRecoil = modifiedGun.getGeneral().getRecoilAngle() * recoilModifier;
        this.progressCameraRecoil = 0F;
        this.gunRecoilRandom = random.nextFloat();
    }

    public void onRenderTick()
    {
        if(this.cameraRecoil <= 0)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        if(!Config.SERVER.enableCameraRecoil.get())
            return;

        float recoilAmount = this.cameraRecoil * mc.getDeltaFrameTime() * 0.15F;
        float startProgress = this.progressCameraRecoil / this.cameraRecoil;
        float endProgress = (this.progressCameraRecoil + recoilAmount) / this.cameraRecoil;

        float pitch = mc.player.getXRot();
        if(startProgress < 0.2F)
        {
            mc.player.setXRot(pitch - ((endProgress - startProgress) / 0.2F) * this.cameraRecoil);
        }
        else
        {
            mc.player.setXRot(pitch + ((endProgress - startProgress) / 0.8F) * this.cameraRecoil);
        }

        this.progressCameraRecoil += recoilAmount;

        if(this.progressCameraRecoil >= this.cameraRecoil)
        {
            this.cameraRecoil = 0;
            this.progressCameraRecoil = 0;
        }
    }

    /**
     * Fabric port of the Forge RenderHandEvent handler (HIGHEST priority). Called from
     * ItemInHandRendererMixin before GunRenderingHandler.onRenderHand.
     */
    public void onRenderHandCooldown(ItemStack heldItem, float frameTime)
    {
        if(!(heldItem.getItem() instanceof GunItem gunItem))
            return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(gunItem, frameTime);
        cooldown = cooldown >= modifiedGun.getGeneral().getRecoilDurationOffset() ? (cooldown - modifiedGun.getGeneral().getRecoilDurationOffset()) / (1.0F - modifiedGun.getGeneral().getRecoilDurationOffset()) : 0.0F;
        if(cooldown >= 0.8)
        {
            float amount = 1.0F * ((1.0F - cooldown) / 0.2F);
            this.gunRecoilNormal = 1 - (--amount) * amount * amount * amount;
        }
        else
        {
            float amount = (cooldown / 0.8F);
            this.gunRecoilNormal = amount < 0.5 ? 2 * amount * amount : -1 + (4 - 2 * amount) * amount;
        }

        this.gunRecoilAngle = modifiedGun.getGeneral().getRecoilAngle();
    }

    public void register()
    {
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.START.register(context -> this.onRenderTick());
    }

    public double getAdsRecoilReduction(Gun gun)
    {
        return 1.0 - gun.getGeneral().getRecoilAdsReduction() * AimingHandler.get().getNormalisedAdsProgress();
    }

    public double getGunRecoilNormal()
    {
        return this.gunRecoilNormal;
    }

    public double getGunRecoilAngle()
    {
        return this.gunRecoilAngle;
    }

    public float getGunRecoilRandom()
    {
        return this.gunRecoilRandom;
    }
}
