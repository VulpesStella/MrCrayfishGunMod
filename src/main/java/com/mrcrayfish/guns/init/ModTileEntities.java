package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.blockentity.WorkbenchBlockEntity;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModTileEntities
{
    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH = register("workbench", WorkbenchBlockEntity::new, () -> new Block[]{ModBlocks.WORKBENCH.get()});

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String id, BlockEntityType.BlockEntitySupplier<T> factoryIn, Supplier<Block[]> validBlocksSupplier)
    {
        return RegistryObject.of(Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(Reference.MOD_ID, id), BlockEntityType.Builder.of(factoryIn, validBlocksSupplier.get()).build(null)));
    }
}
