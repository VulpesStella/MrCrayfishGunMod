package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.crafting.WorkbenchRecipe;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Author: MrCrayfish
 */
public class ModRecipeTypes
{
    public static final RegistryObject<RecipeType<WorkbenchRecipe>> WORKBENCH = create("workbench");

    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> create(String name)
    {
        return RegistryObject.of(new RecipeType<>()
        {
            @Override
            public String toString()
            {
                return name;
            }
        });
    }
}
