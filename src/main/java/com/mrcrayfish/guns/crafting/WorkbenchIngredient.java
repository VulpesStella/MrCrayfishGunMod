package com.mrcrayfish.guns.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Fabric port: vanilla 1.20.1 {@link Ingredient} exposes no extension point (fields
 * are private and subclassing is not possible), so this ingredient is now a
 * composition of a vanilla {@link Ingredient} plus a stack {@link #count}, exactly
 * as specified by plan.md T07. Item matching is delegated to the wrapped
 * ingredient; count handling stays with the caller (InventoryUtil sums matched
 * stacks and compares against {@link #getCount()}).
 *
 * Author: MrCrayfish
 */
public class WorkbenchIngredient
{
    private final Ingredient ingredient;
    private final int count;

    WorkbenchIngredient(Ingredient ingredient, int count)
    {
        if(count <= 0) throw new IllegalArgumentException("Workbench ingredient count must be positive");
        this.ingredient = ingredient;
        this.count = count;
    }

    public int getCount()
    {
        return this.count;
    }

    public Ingredient getIngredient()
    {
        return this.ingredient;
    }

    /**
     * Item-match only; the caller accumulates stack counts against {@link #getCount()}.
     */
    public boolean test(ItemStack stack)
    {
        return this.ingredient.test(stack);
    }

    public ItemStack[] getItems()
    {
        return this.ingredient.getItems();
    }

    /**
     * Mirrors the baseline serialization: {item|tag, count} - the count key is always
     * written (baseline pistol.json carries "count": 14).
     */
    public com.google.gson.JsonObject toJson()
    {
        JsonObject object = this.ingredient.toJson().getAsJsonObject();
        object.addProperty("count", this.count);
        return object;
    }

    public static WorkbenchIngredient fromJson(JsonObject object)
    {
        // Vanilla Ingredient.fromJson ignores the extra "count" key
        return new WorkbenchIngredient(Ingredient.fromJson(object), GsonHelper.getAsInt(object, "count", 1));
    }

    public static WorkbenchIngredient of(ItemLike item, int count)
    {
        return new WorkbenchIngredient(Ingredient.of(item), count);
    }

    public static WorkbenchIngredient of(TagKey<Item> tag, int count)
    {
        return new WorkbenchIngredient(Ingredient.of(tag), count);
    }

    /**
     * Datagen-only reference to a third-party item that is not registered in this
     * environment (baseline {@code UnknownValue}). Vanilla Ingredient cannot express
     * an unregistered item, so this matches nothing at runtime - the same behavior
     * the baseline UnknownValue had (its item list was empty).
     */
    public static WorkbenchIngredient of(ResourceLocation id, int count)
    {
        return new WorkbenchIngredient(Ingredient.EMPTY, count);
    }
}
