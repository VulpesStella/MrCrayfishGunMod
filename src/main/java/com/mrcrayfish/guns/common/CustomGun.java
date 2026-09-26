package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.annotation.Ignored;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class CustomGun
{
    @Ignored
    public ItemStack model;
    public Gun gun;

    public ItemStack getModel()
    {
        return this.model;
    }

    public Gun getGun()
    {
        return this.gun;
    }

        public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        compound.put("Model", this.model.save(new CompoundTag()));
        compound.put("Gun", this.gun.serializeNBT());
        return compound;
    }

        public void deserializeNBT(CompoundTag compound)
    {
        this.model = ItemStack.of(compound.getCompound("Model"));
        this.gun = Gun.create(compound.getCompound("Gun"));
    }
}
