package com.mrcrayfish.guns.client.render.gun;

import com.mrcrayfish.guns.item.GunItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ModelOverrides
{
    private static final Map<Item, IOverrideModel> MODEL_MAP = new HashMap<>();

    /**
     * Registers an override model to the given item.
     *
     * @param item  the item to override it's model
     * @param model a custom IOverrideModel implementation
     */
    public static void register(Item item, IOverrideModel model)
    {
        /* Fabric port note: the baseline also did MinecraftForge.EVENT_BUS.register(model) so
         * override model instances could hold their own @SubscribeEvent handlers (e.g.
         * MiniGunModel#onClientDisconnect). Fabric has no per-instance event bus, so override
         * models that need events register their own Fabric callbacks (see MiniGunModel). */
        MODEL_MAP.putIfAbsent(item, model);
    }

    /**
     * Checks if the given ItemStack has an overridden model
     *
     * @param stack the stack to check
     * @return True if overridden model exists
     */
    public static boolean hasModel(ItemStack stack)
    {
        return MODEL_MAP.containsKey(stack.getItem());
    }

    /**
     * Gets the overridden model for the given ItemStack.
     *
     * @param stack the stack of the overriden model
     * @return The overridden model for the stack or null if no overridden model exists.
     */
    @Nullable
    public static IOverrideModel getModel(ItemStack stack)
    {
        return MODEL_MAP.get(stack.getItem());
    }

    /**
     * Fabric port: was {@code @SubscribeEvent TickEvent.PlayerTickEvent} (Phase.START,
     * LogicalSide.CLIENT). Fabric has no per-player client tick event, so every player of the
     * client world is ticked once per client tick. Must be called during client mod init.
     */
    public static void register()
    {
        ClientTickEvents.START_CLIENT_TICK.register(mc ->
        {
            if(mc.level != null)
            {
                for(Player player : mc.level.players())
                {
                    ModelOverrides.onClientPlayerTick(player);
                }
            }
        });
    }

    static void onClientPlayerTick(Player player)
    {
        tick(player);
    }

    private static void tick(Player player)
    {
        ItemStack heldItem = player.getMainHandItem();
        if(!heldItem.isEmpty() && heldItem.getItem() instanceof GunItem)
        {
            IOverrideModel model = ModelOverrides.getModel(heldItem);
            if(model != null)
            {
                model.tick(player);
            }
        }
    }
}
