package com.mrcrayfish.guns.datagen;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

/**
 * Fabric port: forge convention tags (#forge:glass, #forge:glass_panes) are replaced
 * by Fabric's convention tags (c:glass_blocks, c:glass_panes), which also fixes the
 * missing-tag references observed at runtime for cgm:fragile.
 */
public class BlockTagGen extends FabricTagProvider.BlockTagProvider
{
    public BlockTagGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.getOrCreateTagBuilder(ModTags.Blocks.FRAGILE)
                // c:/minecraft convention tags are not resolvable during datagen
                // validation, so reference them optionally (runtime resolves them via
                // Fabric API's shipped convention tag data)
                .addOptionalTag(ConventionalBlockTags.GLASS_PANES.location())
                .addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS.location())
                .addOptionalTag(BlockTags.CANDLES.location())
                .add(Blocks.LILY_PAD)
                .add(Blocks.COCOA)
                .add(Blocks.END_ROD)
                .add(Blocks.SCAFFOLDING)
                .add(Blocks.SEA_PICKLE)
                .add(Blocks.TURTLE_EGG)
                .add(Blocks.GLOWSTONE)
                .add(Blocks.SEA_LANTERN)
                .add(Blocks.ICE)
                .add(Blocks.BLUE_ICE)
                .add(Blocks.FROSTED_ICE)
                .add(Blocks.PACKED_ICE)
                .add(Blocks.BAMBOO)
                .add(Blocks.SMALL_AMETHYST_BUD)
                .add(Blocks.MEDIUM_AMETHYST_BUD)
                .add(Blocks.LARGE_AMETHYST_BUD)
                .add(Blocks.AMETHYST_CLUSTER)
                .add(Blocks.LANTERN)
                .add(Blocks.SOUL_LANTERN)
                .add(Blocks.POINTED_DRIPSTONE);
    }
}
