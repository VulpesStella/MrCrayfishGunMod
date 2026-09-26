package com.mrcrayfish.guns.datagen;

import com.mrcrayfish.guns.Reference;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ItemTagGen extends FabricTagProvider.ItemTagProvider
{
    public ItemTagGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
    }
}
