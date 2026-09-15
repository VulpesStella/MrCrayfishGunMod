package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.item.attachment.IScope;
import com.mrcrayfish.guns.item.attachment.impl.Scope;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic scope attachment item implementation with color support
 *
 * Author: MrCrayfish
 */
public class ScopeItem extends AttachmentItem implements IScope, IColored
{
    private final Scope scope;
    private final boolean colored;

    public ScopeItem(Scope scope, Item.Properties properties)
    {
        super(properties);
        this.scope = scope;
        this.colored = true;
    }

    public ScopeItem(Scope scope, Item.Properties properties, boolean colored)
    {
        super(properties);
        this.scope = scope;
        this.colored = colored;
    }

    @Override
    public Scope getProperties()
    {
        return this.scope;
    }

    @Override
    public boolean canColor(ItemStack stack)
    {
        return this.colored;
    }

    // Fabric port: Forge canApplyAtEnchantingTable does not exist on vanilla Item;
    // TODO(T08): binding-curse applicability moves into the enchantment hooks per plan.
}
