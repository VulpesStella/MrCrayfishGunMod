package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.Attachments;
import com.mrcrayfish.guns.common.GunModifiers;
import com.mrcrayfish.guns.item.AmmoItem;
import com.mrcrayfish.guns.item.BarrelItem;
import com.mrcrayfish.guns.item.GrenadeItem;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.ScopeItem;
import com.mrcrayfish.guns.item.StockItem;
import com.mrcrayfish.guns.item.StunGrenadeItem;
import com.mrcrayfish.guns.item.UnderBarrelItem;
import com.mrcrayfish.guns.item.attachment.impl.Barrel;
import com.mrcrayfish.guns.item.attachment.impl.Stock;
import com.mrcrayfish.guns.item.attachment.impl.UnderBarrel;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModItems
{
    public static final List<RegistryObject<? extends Item>> ALL_ITEMS = new ArrayList<>();

    /* Weapons */
    public static final RegistryObject<GunItem> PISTOL = register("pistol", () -> new GunItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHOTGUN = register("shotgun", () -> new GunItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RIFLE = register("rifle", () -> new GunItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GRENADE_LAUNCHER = register("grenade_launcher", () -> new GunItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BAZOOKA = register("bazooka", () -> new GunItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MINI_GUN = register("mini_gun", () -> new GunItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<GunItem> ASSAULT_RIFLE = register("assault_rifle", () -> new GunItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MACHINE_PISTOL = register("machine_pistol", () -> new GunItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HEAVY_RIFLE = register("heavy_rifle", () -> new GunItem(new Item.Properties().stacksTo(1)));

    /* Ammo + Throwables */
    public static final RegistryObject<Item> BASIC_BULLET = register("basic_bullet", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> ADVANCED_AMMO = register("advanced_bullet", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> SHELL = register("shell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> MISSILE = register("missile", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> GRENADE = register("grenade", () -> new GrenadeItem(new Item.Properties(), 20 * 4));
    public static final RegistryObject<Item> STUN_GRENADE = register("stun_grenade", () -> new StunGrenadeItem(new Item.Properties(), 72000));

    /* Scope Attachments */
    public static final RegistryObject<Item> SHORT_SCOPE = register("short_scope", () -> new ScopeItem(Attachments.SHORT_SCOPE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MEDIUM_SCOPE = register("medium_scope", () -> new ScopeItem(Attachments.MEDIUM_SCOPE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LONG_SCOPE = register("long_scope", () -> new ScopeItem(Attachments.LONG_SCOPE, new Item.Properties().stacksTo(1)));

    /* Barrel Attachments */
    public static final RegistryObject<Item> SILENCER = register("silencer", () -> new BarrelItem(Barrel.create(8.0F, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE), new Item.Properties().stacksTo(1)));

    /* Stock Attachments */
    public static final RegistryObject<Item> LIGHT_STOCK = register("light_stock", () -> new StockItem(Stock.create(GunModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<Item> TACTICAL_STOCK = register("tactical_stock", () -> new StockItem(Stock.create(GunModifiers.STABILISED), new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<Item> WEIGHTED_STOCK = register("weighted_stock", () -> new StockItem(Stock.create(GunModifiers.SUPER_STABILISED), new Item.Properties().stacksTo(1)));

    /* Under Barrel Attachments */
    public static final RegistryObject<Item> LIGHT_GRIP = register("light_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.LIGHT_RECOIL), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPECIALISED_GRIP = register("specialised_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1)));

    private static <T extends Item> RegistryObject<T> register(String id, Supplier<T> supplier)
    {
        RegistryObject<T> ro = RegistryObject.of(Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Reference.MOD_ID, id), supplier.get()));
        ALL_ITEMS.add(ro);
        return ro;
    }
}
