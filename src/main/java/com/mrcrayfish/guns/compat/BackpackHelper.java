package com.mrcrayfish.guns.compat;

import com.mrcrayfish.backpacked.common.augment.Augments;
import com.mrcrayfish.backpacked.core.ModAugmentTypes;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.common.AmmoContext;
import com.mrcrayfish.guns.common.Gun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Ammo lookup for the Backpacked mod, targeting the Fabric 1.20.1 Backpacked 3.0.9 API.
 *
 * <p><strong>Server only.</strong> The only caller is {@code ReloadTracker}, which runs
 * inside the server tick and skips client-side players. Lookup performs no writes, so even
 * an accidental client call could not consume ammo, but the class is not intended for use
 * on the client.
 *
 * <p>This class references Backpacked types directly, so it must only be loaded when the
 * mod is present and the adapter is enabled. That is enforced by
 * {@code FabricGunMod.backpackedLoaded}, which {@code Gun#findAmmo} checks before touching
 * this class.
 *
 * <p>Why the shape below: {@code BackpackInventory} extends {@code UnlockableContainer},
 * whose {@code getItem} returns {@link ItemStack#EMPTY} for locked slots and the container's
 * own stack object for unlocked ones. So scanning every index cannot reach a locked slot's
 * contents, and the stack handed to {@link AmmoContext} is a live reference that
 * {@code shrink} mutates in place. {@code BackpackInventory#setChanged()} sets an internal
 * dirty flag; Backpacked flushes it into the backpack item's NBT from
 * {@code Player#tick} (via {@code backpacked$TickHead} -> {@code BackpackInventory#tick()})
 * and again on {@code addAdditionalSaveData}. Direct shrink plus {@code setChanged} is
 * therefore the sanctioned path; it is also what Backpacked's own
 * {@code AugmentHandler#locateAmmunition} relies on, since it hands a live stack to vanilla
 * bow code which shrinks it directly.
 */
public class BackpackHelper
{
    /**
     * @return the first backpack slot holding a stack matching {@code id}, or
     *         {@link AmmoContext#NONE}. Never writes.
     */
    public static AmmoContext findAmmo(Player player, ResourceLocation id)
    {
        if(!(player instanceof BackpackedInventoryAccess access))
        {
            return AmmoContext.NONE;
        }

        // Backpacked caches one BackpackInventory per equipped slot on the player, so the
        // stream order is the player's slot order and is stable across calls.
        List<BackpackInventory> inventories = access.backpacked$streamNonNullBackpackInventories().toList();

        boolean needsAugment = Config.COMMON.compatibilities.backpackedNeedsQuiverLink2ReloadFromBackpack.get();

        for(BackpackInventory inventory : inventories)
        {
            if(inventory == null)
            {
                continue;
            }

            ItemStack backpack = inventory.getBackpackStack();
            if(backpack.isEmpty())
            {
                // Nothing equipped in this slot: skip it, do not stop searching.
                continue;
            }

            if(needsAugment && !Augments.cached(backpack).has(ModAugmentTypes.QUIVERLINK.get()))
            {
                // This backpack is not eligible under the current configuration: skip it,
                // do not stop searching. The augment check does not widen the backpack's
                // own slot permissions.
                continue;
            }

            for(int i = 0; i < inventory.getContainerSize(); i++)
            {
                ItemStack stack = inventory.getItem(i);
                if(Gun.isAmmo(stack, id))
                {
                    // Live reference + the container's own dirty notification. The gun is
                    // credited only with what extraction reports as removed.
                    return new AmmoContext(stack, inventory);
                }
            }
        }

        return AmmoContext.NONE;
    }
}
