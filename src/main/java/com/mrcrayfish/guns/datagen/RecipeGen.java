package com.mrcrayfish.guns.datagen;

import com.google.gson.JsonObject;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.crafting.WorkbenchIngredient;
import com.mrcrayfish.guns.crafting.WorkbenchRecipeBuilder;
import com.mrcrayfish.guns.init.ModBlocks;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModRecipeSerializers;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class RecipeGen extends FabricRecipeProvider
{
    public RecipeGen(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer)
    {
        // Dye Item
        consumer.accept(new FinishedRecipe()
        {
            @Override
            public void serializeRecipeData(JsonObject json) {}

            @Override
            public RecipeSerializer<?> getType()
            {
                return ModRecipeSerializers.DYE_ITEM.get();
            }

            @Override
            public ResourceLocation getId()
            {
                return new ResourceLocation(Reference.MOD_ID, "dye_item");
            }

            @Override
            @Nullable
            public JsonObject serializeAdvancement()
            {
                return null;
            }

            @Override
            public ResourceLocation getAdvancementId()
            {
                return null;
            }
        });

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WORKBENCH.get())
                .pattern("CCC")
                .pattern("III")
                .pattern("I I")
                .define('C', Blocks.LIGHT_GRAY_CONCRETE)
                .define('I', ConventionalItemTags.IRON_INGOTS)
                .unlockedBy("has_concrete", has(Blocks.LIGHT_GRAY_CONCRETE))
                .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                .save(consumer);

        // Guns
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.PISTOL.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 14))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.SHOTGUN.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 24))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.RIFLE.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 24))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.GRENADE_LAUNCHER.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 32))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.BAZOOKA.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 44))
                .addIngredient(Items.REDSTONE, 4)
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.RED_DYES, 1))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .addCriterion("has_redstone", has(Items.REDSTONE))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.MINI_GUN.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 38))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.ASSAULT_RIFLE.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 28))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.MACHINE_PISTOL.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 20))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.HEAVY_RIFLE.get())
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.IRON_INGOTS, 36))
                .addCriterion("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
                .build(consumer);

        // Ammo
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.BASIC_BULLET.get(), 64)
                .addIngredient(WorkbenchIngredient.of(Items.COPPER_INGOT, 4))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.GUNPOWDER, 1))
                .addCriterion("has_copper_ingot", has(Items.COPPER_INGOT))
                .addCriterion("has_gunpowder", has(net.minecraft.world.item.Items.GUNPOWDER))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.ADVANCED_AMMO.get(), 32)
                .addIngredient(WorkbenchIngredient.of(Items.COPPER_INGOT, 4))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.GUNPOWDER, 1))
                .addCriterion("has_copper_ingot", has(Items.COPPER_INGOT))
                .addCriterion("has_gunpowder", has(net.minecraft.world.item.Items.GUNPOWDER))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.SHELL.get(), 48)
                .addIngredient(WorkbenchIngredient.of(Items.COPPER_INGOT, 4))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.GOLD_NUGGET, 1))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.GUNPOWDER, 1))
                .addCriterion("has_copper_ingot", has(Items.COPPER_INGOT))
                .addCriterion("has_gold_nugget", has(net.minecraft.world.item.Items.GOLD_NUGGET))
                .addCriterion("has_gunpowder", has(net.minecraft.world.item.Items.GUNPOWDER))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.MISSILE.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 2))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.GUNPOWDER, 4))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_gunpowder", has(net.minecraft.world.item.Items.GUNPOWDER))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.GRENADE.get(), 2)
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 1))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.GUNPOWDER, 4))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_gunpowder", has(net.minecraft.world.item.Items.GUNPOWDER))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.STUN_GRENADE.get(), 2)
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 1))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.GUNPOWDER, 2))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST, 4))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_gunpowder", has(net.minecraft.world.item.Items.GUNPOWDER))
                .addCriterion("has_glowstone", has(net.minecraft.world.item.Items.GLOWSTONE_DUST))
                .build(consumer);

        // Scope Attachments
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.SHORT_SCOPE.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 2))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.AMETHYST_SHARD, 1))
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.REDSTONE_DUSTS, 2))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_amethyst", has(net.minecraft.world.item.Items.AMETHYST_SHARD))
                .addCriterion("has_redstone", has(ConventionalItemTags.REDSTONE_DUSTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.MEDIUM_SCOPE.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 4))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.AMETHYST_SHARD, 1))
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.REDSTONE_DUSTS, 4))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_amethyst", has(net.minecraft.world.item.Items.AMETHYST_SHARD))
                .addCriterion("has_redstone", has(ConventionalItemTags.REDSTONE_DUSTS))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.LONG_SCOPE.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 6))
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.AMETHYST_SHARD, 2))
                .addIngredient(WorkbenchIngredient.of(ConventionalItemTags.BLACK_DYES, 1))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_amethyst", has(net.minecraft.world.item.Items.AMETHYST_SHARD))
                .addCriterion("has_black_dye", has(ConventionalItemTags.BLACK_DYES))
                .build(consumer);

        // Barrel Attachments
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.SILENCER.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 4))
                .addIngredient(WorkbenchIngredient.of(Items.SPONGE, 1))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .build(consumer);

        // Stock Attachments
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.LIGHT_STOCK.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 6))
                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.TACTICAL_STOCK.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 8))
                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.WEIGHTED_STOCK.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 12))
                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
                .build(consumer);

        // Under Barrel Attachments
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.LIGHT_GRIP.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 4))
                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
                .build(consumer);
        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ModItems.SPECIALISED_GRIP.get())
                .addIngredient(WorkbenchIngredient.of(net.minecraft.world.item.Items.IRON_NUGGET, 8))
                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
                .addCriterion("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_NUGGET))
                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
                .build(consumer);
    }
}