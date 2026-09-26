package com.mrcrayfish.guns.datagen;

import com.mrcrayfish.guns.init.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

/**
 * Fabric port: FabricBlockLootTableProvider replaces the Forge
 * LootTableProvider/BlockLootSubProvider pair; the known-blocks filter is not needed
 * because Fabric validates against the tables this provider actually emits.
 */
public class LootTableGen extends FabricBlockLootTableProvider
{
    public LootTableGen(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public void generate()
    {
        this.dropSelf(ModBlocks.WORKBENCH.get());
    }
}
