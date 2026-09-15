package com.mrcrayfish.guns.init;

import com.mojang.serialization.Codec;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.particles.BulletHoleData;
import com.mrcrayfish.guns.particles.TrailData;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModParticleTypes
{
    // Vanilla 1.20.1 keeps the SimpleParticleType constructor protected (Forge opened it)
    private static class Simple extends SimpleParticleType
    {
        private Simple(boolean overrideLimiter)
        {
            super(overrideLimiter);
        }
    }

    public static final RegistryObject<ParticleType<BulletHoleData>> BULLET_HOLE = register("bullet_hole",() -> new ParticleType<>(false, BulletHoleData.DESERIALIZER)
    {
        @Override
        public Codec<BulletHoleData> codec()
        {
            return BulletHoleData.CODEC;
        }
    });
    public static final RegistryObject<SimpleParticleType> BLOOD = register("blood", () -> new Simple(true));
    public static final RegistryObject<ParticleType<TrailData>> TRAIL = register("trail", () -> new ParticleType<>(false, TrailData.DESERIALIZER)
    {
        @Override
        public Codec<TrailData> codec()
        {
            return TrailData.CODEC;
        }
    });

    private static <T extends ParticleType<?>> RegistryObject<T> register(String id, Supplier<T> supplier)
    {
        return RegistryObject.of(Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(Reference.MOD_ID, id), supplier.get()));
    }
}
