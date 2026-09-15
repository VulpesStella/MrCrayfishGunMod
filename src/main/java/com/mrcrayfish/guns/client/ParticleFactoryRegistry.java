package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.client.particle.BloodParticle;
import com.mrcrayfish.guns.client.particle.BulletHoleParticle;
import com.mrcrayfish.guns.client.particle.TrailParticle;
import com.mrcrayfish.guns.init.ModParticleTypes;

/**
 * Author: MrCrayfish
 */
public class ParticleFactoryRegistry
{
    /**
     * Fabric port: was {@code @SubscribeEvent RegisterParticleProvidersEvent} (mod bus).
     * Mapped to Fabric's {@link net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry}
     * (same simple name as this class, hence the FQN): the no-sprite factory registers
     * directly, the sprite set factories register as a pending factory that receives the
     * SpriteSet when the particle engine initializes.
     *
     * <p>Must be called during client mod init, before vanilla registers particle providers.</p>
     */
    public static void register()
    {
        net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.getInstance().register(ModParticleTypes.BULLET_HOLE.get(), (typeIn, worldIn, x, y, z, xSpeed, ySpeed, zSpeed) -> new BulletHoleParticle(worldIn, x, y, z, typeIn.getDirection(), typeIn.getPos()));
        net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.getInstance().register(ModParticleTypes.BLOOD.get(), sprites -> new BloodParticle.Factory(sprites));
        net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.getInstance().register(ModParticleTypes.TRAIL.get(), sprites -> new TrailParticle.Factory(sprites));
    }
}
