package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.enchantment.AcceleratorEnchantment;
import com.mrcrayfish.guns.enchantment.CollateralEnchantment;
import com.mrcrayfish.guns.enchantment.FireStarterEnchantment;
import com.mrcrayfish.guns.enchantment.LightweightEnchantment;
import com.mrcrayfish.guns.enchantment.OverCapacityEnchantment;
import com.mrcrayfish.guns.enchantment.PuncturingEnchantment;
import com.mrcrayfish.guns.enchantment.QuickHandsEnchantment;
import com.mrcrayfish.guns.enchantment.ReclaimedEnchantment;
import com.mrcrayfish.guns.enchantment.TriggerFingerEnchantment;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModEnchantments
{
    public static final RegistryObject<Enchantment> QUICK_HANDS = register("quick_hands", QuickHandsEnchantment::new);
    public static final RegistryObject<Enchantment> TRIGGER_FINGER = register("trigger_finger", TriggerFingerEnchantment::new);
    public static final RegistryObject<Enchantment> LIGHTWEIGHT = register("lightweight", LightweightEnchantment::new);
    public static final RegistryObject<Enchantment> COLLATERAL = register("collateral", CollateralEnchantment::new);
    public static final RegistryObject<Enchantment> OVER_CAPACITY = register("over_capacity", OverCapacityEnchantment::new);
    public static final RegistryObject<Enchantment> RECLAIMED = register("reclaimed", ReclaimedEnchantment::new);
    public static final RegistryObject<Enchantment> ACCELERATOR = register("accelerator", AcceleratorEnchantment::new);
    public static final RegistryObject<Enchantment> PUNCTURING = register("puncturing", PuncturingEnchantment::new);
    public static final RegistryObject<Enchantment> FIRE_STARTER = register("fire_starter", FireStarterEnchantment::new);

    private static <T extends Enchantment> RegistryObject<T> register(String id, Supplier<T> supplier)
    {
        return RegistryObject.of(Registry.register(BuiltInRegistries.ENCHANTMENT, new ResourceLocation(Reference.MOD_ID, id), supplier.get()));
    }
}
