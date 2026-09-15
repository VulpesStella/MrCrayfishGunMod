package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.item.attachment.IBarrel;
import com.mrcrayfish.guns.item.attachment.impl.Barrel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic barrel attachment item implementation with color support
 *
 * Author: MrCrayfish
 */
public class BarrelItem extends AttachmentItem implements IBarrel, IColored
{
    private final Barrel barrel;
    private final boolean colored;

    public BarrelItem(Barrel barrel, Item.Properties properties)
    {
        super(properties);
        this.barrel = barrel;
        this.colored = true;
    }

    public BarrelItem(Barrel barrel, Item.Properties properties, boolean colored)
    {
        super(properties);
        this.barrel = barrel;
        this.colored = colored;
    }

    @Override
    public Barrel getProperties()
    {
        return this.barrel;
    }

    @Override
    public boolean canColor(ItemStack stack)
    {
        return this.colored;
    }

    // Fabric port: Forge canApplyAtEnchantingTable does not exist on vanilla Item;
    // TODO(T08): binding-curse applicability moves into the enchantment hooks per plan.
}
