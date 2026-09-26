package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.entity.GrenadeEntity;
import com.mrcrayfish.guns.entity.MissileEntity;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.entity.ThrowableGrenadeEntity;
import com.mrcrayfish.guns.entity.ThrowableStunGrenadeEntity;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModEntities
{
    public static final RegistryObject<EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("projectile", ProjectileEntity::new);
    public static final RegistryObject<EntityType<GrenadeEntity>> GRENADE = registerBasic("grenade", GrenadeEntity::new);
    public static final RegistryObject<EntityType<MissileEntity>> MISSILE = registerBasic("missile", MissileEntity::new);
    public static final RegistryObject<EntityType<ThrowableGrenadeEntity>> THROWABLE_GRENADE = registerBasic("throwable_grenade", ThrowableGrenadeEntity::new);
    public static final RegistryObject<EntityType<ThrowableStunGrenadeEntity>> THROWABLE_STUN_GRENADE = registerBasic("throwable_stun_grenade", ThrowableStunGrenadeEntity::new);

    private static <T extends Entity> RegistryObject<EntityType<T>> registerBasic(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return register(id, () -> FabricEntityTypeBuilder.<T>create(MobCategory.MISC, function::apply)
                .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                .trackRangeBlocks(100)
                .trackedUpdateRate(1)
                .forceTrackedVelocityUpdates(true)
                .disableSummon()
                .fireImmune()
                .build());
    }

    /**
     * Entity registration that prevents the entity from being sent and tracked by clients. Projectiles
     * are rendered separately from Minecraft's entity rendering system and their logic is handled
     * exclusively by the server, why send them to the client. Projectiles also have very short time
     * in the world and are spawned many times a tick. There is no reason to send unnecessary packets
     * when it can be avoided to drastically improve the performance of the game.
     *
     * TODO(T04): Forge used setCustomClientFactory here; the Fabric spawn-data strategy must
     *  cover client-side projectile data (see plan.md T04 step 6-10).
     *
     * @param id       the id of the projectile
     * @param function the factory to spawn the projectile for the server
     * @param <T>      an entity that is a projectile entity
     * @return A registry object containing the new entity type
     */
    private static <T extends ProjectileEntity> RegistryObject<EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return register(id, () -> FabricEntityTypeBuilder.<T>create(MobCategory.MISC, function::apply)
                .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                .trackRangeBlocks(0)
                .trackedUpdateRate(1)
                .forceTrackedVelocityUpdates(false)
                .disableSummon()
                .fireImmune()
                .build());
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, Supplier<EntityType<T>> supplier)
    {
        return RegistryObject.of(Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(Reference.MOD_ID, id), supplier.get()));
    }
}
