package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.block.WorkbenchBlock;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModBlocks {
    public static final RegistryObject<Block> WORKBENCH = register("workbench", () -> new WorkbenchBlock(Block.Properties.of().strength(1.5F).sound(SoundType.METAL).mapColor(MapColor.METAL)));

    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> blockSupplier) {
        return register(id, blockSupplier, block1 -> new BlockItem(block1, new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> blockSupplier, @Nullable Function<T, BlockItem> supplier) {
        T block = blockSupplier.get();
        RegistryObject<T> registryObject = RegistryObject.of(Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(Reference.MOD_ID, id), block));
        if (supplier != null) {
            Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Reference.MOD_ID, id), supplier.apply(block));
        }
        return registryObject;
    }
}
