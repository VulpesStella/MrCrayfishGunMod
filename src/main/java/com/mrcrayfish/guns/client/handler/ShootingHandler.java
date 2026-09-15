package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.FabricGunMod;
import com.mrcrayfish.guns.client.KeyBinds;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.compat.PlayerReviveHelper;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageShoot;
import com.mrcrayfish.guns.network.message.C2SMessageShooting;
import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Author: MrCrayfish
 */
public class ShootingHandler
{
    private static ShootingHandler instance;

    public static ShootingHandler get()
    {
        if(instance == null)
        {
            instance = new ShootingHandler();
        }
        return instance;
    }

    private boolean shooting;

    private ShootingHandler() {}

    private boolean isInGame()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.getOverlay() != null)
            return false;
        if(mc.screen != null)
            return false;
        if(!mc.mouseHandler.isMouseGrabbed())
            return false;
        return mc.isWindowActive();
    }

    /**
     * Fabric port of the InputEvent.InteractionKeyMappingTriggered attack branch.
     * Called from MinecraftMixin#startAttack/continueAttack. Returns true when the
     * vanilla attack/swing must be canceled because the gun fires instead.
     */
    public boolean onAttackClick()
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return false;

        if(PlayerReviveHelper.isBleeding(player))
            return false;

        if(Config.CLIENT.controls.flipControls.get() && player.getMainHandItem().getItem() instanceof GunItem)
            return true;

        if(!Config.CLIENT.controls.flipControls.get())
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem gunItem)
            {
                this.fire(player, heldItem);
                Gun gun = gunItem.getModifiedGun(heldItem);
                if(!gun.getGeneral().isAuto())
                {
                    KeyBinds.getShootMapping().setDown(false);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Fabric port of the use-item branch. Called from MinecraftMixin#startUseItem.
     * Returns true when vanilla use behavior must be canceled while holding a gun.
     */
    public boolean onUseClick()
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return false;

        if(PlayerReviveHelper.isBleeding(player))
            return false;

        if(Config.CLIENT.controls.flipControls.get())
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem gunItem)
            {
                this.fire(player, heldItem);
                Gun gun = gunItem.getModifiedGun(heldItem);
                if(!gun.getGeneral().isAuto())
                {
                    KeyBinds.getShootMapping().setDown(false);
                }
                return true;
            }
            return false;
        }

        ItemStack heldItem = player.getMainHandItem();
        if(heldItem.getItem() instanceof GunItem gunItem)
        {
            if(!AimingHandler.get().isZooming() && AimingHandler.get().isLookingAtInteractableBlock())
                return false;
            // Allow shields to be used if weapon is one-handed
            if(player.getOffhandItem().getItem() == Items.SHIELD)
            {
                Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                if(modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED)
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void register()
    {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK.register(mc -> this.onHandleShooting());
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> this.onPostClientTick());
    }

    public void onHandleShooting()
    {
        if(!this.isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player != null)
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem && (Gun.hasAmmo(heldItem) || player.isCreative()) && !PlayerReviveHelper.isBleeding(player))
            {
                boolean shooting = KeyBinds.getShootMapping().isDown();
                if(FabricGunMod.controllableLoaded)
                {
                    // TODO(T11): Controllable controller shooting hook (T11).
                    shooting |= false;
                }
                if(shooting)
                {
                    if(!this.shooting)
                    {
                        this.shooting = true;
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(true));
                    }
                }
                else if(this.shooting)
                {
                    this.shooting = false;
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
                }
            }
            else if(this.shooting)
            {
                this.shooting = false;
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
            }
        }
        else
        {
            this.shooting = false;
        }
    }

    public void onPostClientTick()
    {

        if(!isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player != null)
        {
            if(PlayerReviveHelper.isBleeding(player))
                return;

            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem)
            {
                if(KeyBinds.getShootMapping().isDown())
                {
                    Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                    if(gun.getGeneral().isAuto())
                    {
                        this.fire(player, heldItem);
                    }
                }
            }
        }
    }

    public void fire(Player player, ItemStack heldItem)
    {
        if(!(heldItem.getItem() instanceof GunItem))
            return;

        if(!Gun.hasAmmo(heldItem) && !player.isCreative())
            return;
        
        if(player.isSpectator())
            return;

        if(player.getUseItem().getItem() == Items.SHIELD)
            return;

        ItemCooldowns tracker = player.getCooldowns();
        if(!tracker.isOnCooldown(heldItem.getItem()))
        {
            GunItem gunItem = (GunItem) heldItem.getItem();
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);

            if(GunFireEvent.firePre(player, heldItem))
                return;

            int rate = GunEnchantmentHelper.getRate(heldItem, modifiedGun);
            rate = GunModifierHelper.getModifiedRate(heldItem, rate);
            tracker.addCooldown(heldItem.getItem(), rate);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(player));

            GunFireEvent.firePost(player, heldItem);
        }
    }
}
