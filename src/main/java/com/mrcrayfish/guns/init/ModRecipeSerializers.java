package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.crafting.DyeItemRecipe;
import com.mrcrayfish.guns.crafting.WorkbenchRecipeSerializer;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModRecipeSerializers
{
    public static final RegistryObject<SimpleCraftingRecipeSerializer<DyeItemRecipe>> DYE_ITEM = register("dye_item", () -> new SimpleCraftingRecipeSerializer<>(DyeItemRecipe::new));
    public static final RegistryObject<WorkbenchRecipeSerializer> WORKBENCH = register("workbench", WorkbenchRecipeSerializer::new);

    private static <T extends RecipeSerializer<?>> RegistryObject<T> register(String id, Supplier<T> supplier)
    {
        return RegistryObject.of(Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(Reference.MOD_ID, id), supplier.get()));
    }
}
