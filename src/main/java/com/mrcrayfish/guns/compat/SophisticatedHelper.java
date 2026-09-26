package com.mrcrayfish.guns.compat;

import com.mrcrayfish.guns.common.AmmoContext;
import com.mrcrayfish.guns.common.Gun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/**
 * Ammo lookup for Sophisticated Backpacks, targeting the Fabric 1.20.1 Salandora port
 * (Backpacks 3.23.x + Core 1.2.7.x).
 *
 * <p><strong>Server only.</strong> The only caller is {@code ReloadTracker}, which runs inside the
 * server tick and skips client-side players.
 *
 * <p>This class references Sophisticated types directly, so it must only be loaded when both mods
 * are present. That is enforced by {@code FabricGunMod.sopLoaded}, which {@code Gun#findAmmo}
 * checks before touching this class.
 *
 * <p>Backpacks are found wherever the mod's own provider looks: the player's main inventory,
 * offhand and chest armour slot. {@code PlayerInventoryProvider} keeps its handlers in a
 * {@link java.util.LinkedHashMap}, so the traversal order is {@code main}, {@code offhand},
 * {@code armor} and is stable across calls.
 *
 * <p><strong>The callback's return value means "stop", not "keep going".</strong> Disassembling
 * {@code PlayerInventoryProvider#runOnBackpacks} shows the boolean is consumed as
 * {@code ifeq continue / return}: returning {@code true} ends the whole traversal. The Forge
 * baseline returned {@code true} on both the hit and the miss path, so the first backpack without
 * ammo silently ended the search and later backpacks were never consulted. This implementation
 * returns {@code true} only after a source has been selected and {@code false} otherwise, so a
 * backpack with no ammo cannot hide the ones behind it.
 *
 * <p>Extraction uses the partition-aware transfer API. A slot setter is not an extraction:
 * special partitions can ignore it or expose synthetic stacks (infinity upgrades). Committing
 * the transaction invokes the mod's own persistence and index callbacks. Only the amount
 * actually supplied by that API is credited to the gun, including legitimate infinite sources.
 */
public class SophisticatedHelper
{
    /** {@code InventoryHandler#getSlots()} returns storage views, not a count. */
    private static int slotCount(InventoryHandler inventory)
    {
        return inventory.getSlotCount();
    }

    /**
     * @return the first backpack slot holding a stack matching {@code id}, or
     *         {@link AmmoContext#NONE}. Never writes.
     */
    public static AmmoContext findAmmo(Player player, ResourceLocation id)
    {
        final AmmoContext[] found = {AmmoContext.NONE};

        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, slot) -> {
            BackpackContext.Item backpackContext = new BackpackContext.Item(inventoryName, identifier, slot);
            InventoryHandler inventory = backpackContext.getBackpackWrapper(player).getInventoryHandler();

            for(int i = 0; i < slotCount(inventory); i++)
            {
                ItemStack stack = inventory.getStackInSlot(i);
                if(Gun.isAmmo(stack, id))
                {
                    int slotIndex = i;
                    ItemVariant variant = ItemVariant.of(stack);
                    found[0] = new AmmoContext(stack, requested -> extract(inventory, slotIndex, variant, requested));
                    // A source was selected: end the traversal.
                    return true;
                }
            }
            // Nothing usable here: let the provider continue with the next backpack.
            return false;
        });

        return found[0];
    }

    /**
     * Obtains up to {@code requested} ammo through the mod's partition-aware extraction path.
     *
     * <p>The slot is re-read instead of trusting the context's stack, so a stale context cannot
     * cause a write based on a count that has since changed.
     *
     * @return the amount actually removed
     */
    private static int extract(InventoryHandler inventory, int slot, ItemVariant variant, int requested)
    {
        if(requested <= 0)
        {
            return 0;
        }
        ItemStack current = inventory.getStackInSlot(slot);
        if(current.isEmpty() || !variant.matches(current))
        {
            return 0;
        }
        try(Transaction transaction = Transaction.openOuter())
        {
            long removed = inventory.extractSlot(slot, variant, requested, transaction);
            if(removed < 0 || removed > requested)
            {
                throw new IllegalStateException("Invalid Sophisticated extraction result: " + removed);
            }
            transaction.commit();
            return (int) removed;
        }
    }
}
