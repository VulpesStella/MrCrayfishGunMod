package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.effect.IncurableEffect;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModEffects
{
    public static final RegistryObject<IncurableEffect> BLINDED = register("blinded", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<IncurableEffect> DEAFENED = register("deafened", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));

    private static <T extends MobEffect> RegistryObject<T> register(String id, Supplier<T> supplier)
    {
        return RegistryObject.of(Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(Reference.MOD_ID, id), supplier.get()));
    }
}
