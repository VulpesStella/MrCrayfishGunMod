package com.mrcrayfish.guns.common;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Result of an ammo lookup: the candidate stack together with the single mutation
 * point that removes ammo from the real source.
 *
 * <p>The lookup itself must never write. Removal happens only through
 * {@link #extractor()}, and the caller credits the gun with exactly the amount the
 * extractor reports as removed. That ordering is what keeps
 * "gun gained == source lost" true for live references, for sources that have to
 * write back a copy, and for sources that may refuse an extraction.
 *
 * <p>Author: MrCrayfish
 */
public record AmmoContext(ItemStack stack, AmmoExtractor extractor)
{
    /**
     * Removes up to {@code requested} ammo from the real source.
     *
     * @return the amount actually removed, never more than {@code requested} and
     *         never negative. Zero means the source refused and nothing changed.
     */
    @FunctionalInterface
    public interface AmmoExtractor
    {
        int extract(int requested);
    }

    /** No source. Also used by adapters for mods that are not loaded or enabled. */
    public static final AmmoContext NONE = new AmmoContext(ItemStack.EMPTY, requested -> 0);

    /**
     * Live-reference source: the stack handed to this context is the container's own
     * object, so shrinking it mutates the slot directly and the container only needs
     * to be told that its contents changed.
     */
    public AmmoContext(ItemStack stack, Container container)
    {
        this(stack, amount -> {
            int removed = Math.min(amount, stack.getCount());
            if(removed > 0)
            {
                stack.shrink(removed);
                container.setChanged();
            }
            return removed;
        });
    }

    /**
     * Source that grants the request without consuming anything, matching creative
     * mode's pre-existing behaviour of loading a gun from nothing. Deliberately an
     * explicit factory: a context that removes nothing must never be built by accident.
     */
    public static AmmoContext infinite(ItemStack stack)
    {
        return new AmmoContext(stack, requested -> requested);
    }

    /** True when no ammo source was found. Checks the stack, not object identity. */
    public boolean isEmpty()
    {
        return this.stack.isEmpty();
    }

    /** Removes up to {@code requested} ammo. See {@link AmmoExtractor#extract(int)}. */
    public int extract(int requested)
    {
        if(this.isEmpty() || requested <= 0)
        {
            return 0;
        }
        return Math.max(0, Math.min(requested, this.extractor.extract(requested)));
    }
}
