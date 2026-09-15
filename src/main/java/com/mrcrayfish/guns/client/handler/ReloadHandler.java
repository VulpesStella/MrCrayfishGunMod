package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.client.KeyBinds;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.event.GunReloadEvent;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageReload;
import com.mrcrayfish.guns.network.message.C2SMessageUnload;
import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class ReloadHandler
{
    private static ReloadHandler instance;

    public static ReloadHandler get()
    {
        if(instance == null)
        {
            instance = new ReloadHandler();
        }
        return instance;
    }

    private int startReloadTick;
    private int reloadTimer;
    private int prevReloadTimer;
    private int reloadingSlot;

    private ReloadHandler()
    {
    }

    /**
     * Fabric port: registers the Fabric event hooks that replaced the Forge
     * {@code @SubscribeEvent} handlers (Forge: {@code MinecraftForge.EVENT_BUS.register(this)}).
     */
    public static void register()
    {
        // Forge: TickEvent.ClientTickEvent(Phase.END)
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ReloadHandler.get().handleKeys(mc);
            ReloadHandler.get().onClientTick();
        });
    }

    void onClientTick()
    {
        this.prevReloadTimer = this.reloadTimer;

        Player player = Minecraft.getInstance().player;
        if(player != null)
        {
            if(ModSyncedDataKeys.RELOADING.getValue(player))
            {
                if(this.reloadingSlot != player.getInventory().selected || this.isReloadFinished(player))
                {
                    this.setReloading(false);
                }
            }

            this.updateReloadTimer(player);
        }
    }

    private void handleKeys(Minecraft client)
    {
        boolean active = client.player != null && client.screen == null && client.isWindowActive();
        while(KeyBinds.KEY_RELOAD.consumeClick())
        {
            if(active)
                this.setReloading(!ModSyncedDataKeys.RELOADING.getValue(client.player));
        }
        while(KeyBinds.KEY_UNLOAD.consumeClick())
        {
            if(active)
            {
                this.setReloading(false);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
            }
        }
    }

    public void setReloading(boolean reloading)
    {
        Player player = Minecraft.getInstance().player;
        if(player != null)
        {
            if(reloading)
            {
                ItemStack stack = player.getMainHandItem();
                if(stack.getItem() instanceof GunItem)
                {
                    CompoundTag tag = stack.getTag();
                    if(tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE))
                    {
                        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                        if(tag.getInt("AmmoCount") >= GunEnchantmentHelper.getAmmoCapacity(stack, gun))
                            return;
                        if(GunReloadEvent.firePre(player, stack))
                            return;
                        ModSyncedDataKeys.RELOADING.setValue(player, true);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true));
                        this.reloadingSlot = player.getInventory().selected;
                        GunReloadEvent.firePost(player, stack);
                    }
                }
            }
            else
            {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                this.reloadingSlot = -1;
            }
        }
    }

    /**
     * Client-side safety net for the case where the server's "reload finished" sync never
     * reaches this client (e.g. a dropped/desynced {@code RELOADING} update). The held gun's
     * ammo count is synced through the normal inventory updates, so we can independently detect
     * that the magazine is full (or that the held item is no longer a gun) and stop the reload
     * ourselves, instead of getting stuck in the reload animation until the player switches items.
     *
     * @param player the client player
     * @return true if the reload should be stopped
     */
    private boolean isReloadFinished(Player player)
    {
        ItemStack stack = player.getMainHandItem();
        if(!(stack.getItem() instanceof GunItem))
            return true;
        CompoundTag tag = stack.getTag();
        if(tag == null || tag.contains("IgnoreAmmo", Tag.TAG_BYTE))
            return false;
        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        return tag.getInt("AmmoCount") >= GunEnchantmentHelper.getAmmoCapacity(stack, gun);
    }

    private void updateReloadTimer(Player player)
    {
        if(ModSyncedDataKeys.RELOADING.getValue(player))
        {
            if(this.startReloadTick == -1)
            {
                this.startReloadTick = player.tickCount + 5;
            }
            if(this.reloadTimer < 5)
            {
                this.reloadTimer++;
            }
        }
        else
        {
            if(this.startReloadTick != -1)
            {
                this.startReloadTick = -1;
            }
            if(this.reloadTimer > 0)
            {
                this.reloadTimer--;
            }
        }
    }

    public int getStartReloadTick()
    {
        return this.startReloadTick;
    }

    public int getReloadTimer()
    {
        return this.reloadTimer;
    }

    public float getReloadProgress(float partialTicks)
    {
        return (this.prevReloadTimer + (this.reloadTimer - this.prevReloadTimer) * partialTicks) / 5F;
    }
}
