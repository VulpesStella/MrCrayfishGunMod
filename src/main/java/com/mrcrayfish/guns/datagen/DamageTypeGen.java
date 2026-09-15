package com.mrcrayfish.guns.datagen;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.init.ModDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public class DamageTypeGen extends FabricTagProvider<DamageType>
{
    public DamageTypeGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, Registries.DAMAGE_TYPE, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.getOrCreateTagBuilder(DamageTypeTags.IS_PROJECTILE).add(ModDamageTypes.BULLET);
    }
}
