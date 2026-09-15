package com.mrcrayfish.guns.compat;

import com.mrcrayfish.guns.common.AmmoContext;
import com.mrcrayfish.guns.common.Gun;
import com.tiviacz.travelersbackpack.component.ComponentUtils;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.handler.ItemStackHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Ammo lookup for the Traveler's Backpack mod, targeting the Fabric 1.20.1 9.1.x API.
 *
 * <p><strong>Server only.</strong> The only caller is {@code ReloadTracker}, which runs inside
 * the server tick and skips client-side players.
 *
 * <p>This class references Traveler's Backpack types directly, so it must only be loaded when the
 * mod is present and the adapter is enabled. That is enforced by
 * {@code FabricGunMod.travelersBackpackLoaded}, which {@code Gun#findAmmo} checks before
 * touching this class.
 *
 * <p>Scope matches the Forge baseline: the backpack the player is <em>wearing</em>, main storage
 * only. No scanning of backpacks on the ground, on other entities, or nested inside items, and no
 * tool or upgrade slots.
 *
 * <p><strong>Why the extraction has to write back explicitly:</strong> Traveler's Backpack's
 * {@code ItemStackHandler} returns live stack objects from {@code getStackInSlot}, but its
 * {@code onContentsChanged} and {@code setChanged} are both empty methods. Shrinking the stack
 * therefore mutates the in-memory handler and changes nothing in the backpack item. Durability
 * comes from {@link BackpackWrapper#setSlotChanged(int, ItemStack, int)} with type
 * {@code 0} (main inventory), which serialises the stack straight into the backpack item's
 * {@code Inventory} NBT. That is the same call the Forge baseline used, and it is why this
 * source cannot rely on the generic "shrink plus dirty notification" path.
 */
public class TravelersBackpackHelper
{
    /** {@code BackpackWrapper#setSlotChanged} section selector for the main inventory. */
    private static final int STORAGE_SECTION = 0;

    /**
     * @return the first main-storage slot holding a stack matching {@code id}, or
     *         {@link AmmoContext#NONE}. Never writes.
     */
    public static AmmoContext findAmmo(Player player, ResourceLocation id)
    {
        // Returns null when the player is not wearing a backpack, when the entity component is
        // absent, or when the equipped item is not a Traveler's Backpack. Null is the "no source"
        // signal for this API; no stale player or level reference is retained.
        BackpackWrapper wrapper = ComponentUtils.getBackpackWrapper(player);
        if(wrapper == null)
        {
            return AmmoContext.NONE;
        }

        ItemStackHandler storage = wrapper.getStorage();
        if(storage == null)
        {
            return AmmoContext.NONE;
        }

        for(int slot = 0; slot < storage.getSlots(); slot++)
        {
            ItemStack stack = storage.getStackInSlot(slot);
            if(Gun.isAmmo(stack, id))
            {
                int index = slot;
                // Live reference, but the backpack item only learns about the change through
                // setSlotChanged. Shrinking without it would lose the ammo on the next load.
                return new AmmoContext(stack, amount -> {
                    int removed = Math.min(amount, stack.getCount());
                    if(removed > 0)
                    {
                        stack.shrink(removed);
                        wrapper.setSlotChanged(index, stack, STORAGE_SECTION);
                    }
                    return removed;
                });
            }
        }

        return AmmoContext.NONE;
    }
}
