package com.mrcrayfish.guns.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrcrayfish.guns.init.ModRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Ocelot
 */
public class WorkbenchRecipeBuilder
{
    @Nullable
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<WorkbenchIngredient> ingredients;
    private final Advancement.Builder advancementBuilder;

    private WorkbenchRecipeBuilder(@Nullable RecipeCategory category, ItemLike item, int count)
    {
        this.category = category;
        this.result = item.asItem();
        this.count = count;
        this.ingredients = new ArrayList<>();
        this.advancementBuilder = Advancement.Builder.advancement();
    }

    public static WorkbenchRecipeBuilder crafting(ItemLike item)
    {
        return new WorkbenchRecipeBuilder(null, item, 1);
    }

    public static WorkbenchRecipeBuilder crafting(ItemLike item, int count)
    {
        return new WorkbenchRecipeBuilder(null, item, count);
    }

    public static WorkbenchRecipeBuilder crafting(@Nullable RecipeCategory category, ItemLike item)
    {
        return new WorkbenchRecipeBuilder(category, item, 1);
    }

    public static WorkbenchRecipeBuilder crafting(@Nullable RecipeCategory category, ItemLike item, int count)
    {
        return new WorkbenchRecipeBuilder(category, item, count);
    }

    public WorkbenchRecipeBuilder addIngredient(ItemLike item, int count)
    {
        this.ingredients.add(WorkbenchIngredient.of(item, count));
        return this;
    }

    public WorkbenchRecipeBuilder addIngredient(WorkbenchIngredient ingredient)
    {
        this.ingredients.add(ingredient);
        return this;
    }

    public WorkbenchRecipeBuilder addCriterion(String name, CriterionTriggerInstance criterionIn)
    {
        this.advancementBuilder.addCriterion(name, criterionIn);
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumer)
    {
        ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(this.result);
        this.build(consumer, resourcelocation);
    }

    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id)
    {
        this.validate(id);
        this.advancementBuilder.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
        consumer.accept(new WorkbenchRecipeBuilder.Result(id, this.result, this.count, this.ingredients, this.advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + (this.category != null ? this.category.getFolderName() : "") + "/" + id.getPath())));
    }

    /**
     * Makes sure that this recipe is valid and obtainable.
     */
    private void validate(ResourceLocation id)
    {
        if(this.advancementBuilder.getCriteria().isEmpty())
        {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    public static class Result implements FinishedRecipe
    {
        private final ResourceLocation id;
        private final Item item;
        private final int count;
        private final List<WorkbenchIngredient> ingredients;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, ItemLike item, int count, List<WorkbenchIngredient> ingredients, Advancement.Builder advancement, ResourceLocation advancementId)
        {
            this.id = id;
            this.item = item.asItem();
            this.count = count;
            this.ingredients = ingredients;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(JsonObject json)
        {
            // Fabric port: forge:conditions serialization dropped (no condition system on
            // Fabric 1.20.1; resource conditions are a T10+ decision if ever needed)
            JsonArray materials = new JsonArray();
            this.ingredients.forEach(ingredient -> materials.add(ingredient.toJson()));
            json.add("materials", materials);

            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.item)).toString());
            if(this.count > 1)
            {
                resultObject.addProperty("count", this.count);
            }
            json.add("result", resultObject);
        }

        @Override
        public ResourceLocation getId()
        {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType()
        {
            return ModRecipeSerializers.WORKBENCH.get();
        }

        @Override
        public JsonObject serializeAdvancement()
        {
            return this.advancement.serializeToJson();
        }

        @Override
        public ResourceLocation getAdvancementId()
        {
            return this.advancementId;
        }
    }
}
