package com.mrcrayfish.guns;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.guns.client.CustomGunManager;
import com.mrcrayfish.guns.common.BoundingBoxManager;
import com.mrcrayfish.guns.common.CustomGunLoader;
import com.mrcrayfish.guns.common.DelayedTask;
import com.mrcrayfish.guns.common.NetworkGunManager;
import com.mrcrayfish.guns.common.ProjectileManager;
import com.mrcrayfish.guns.common.ReloadTracker;
import com.mrcrayfish.guns.common.SpreadTracker;
import com.mrcrayfish.guns.entity.GrenadeEntity;
import com.mrcrayfish.guns.entity.MissileEntity;
import com.mrcrayfish.guns.init.ModBlocks;
import com.mrcrayfish.guns.init.ModContainers;
import com.mrcrayfish.guns.init.ModEffects;
import com.mrcrayfish.guns.init.ModEnchantments;
import com.mrcrayfish.guns.init.ModEntities;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModParticleTypes;
import com.mrcrayfish.guns.init.ModRecipeSerializers;
import com.mrcrayfish.guns.init.ModRecipeTypes;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.init.ModTileEntities;
import com.mrcrayfish.guns.entity.ISpawnDataEntity;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageSpawnData;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fabric main entrypoint (replaces the Forge {@code GunMod} entry class).
 *
 * TODO(T03): event registrations (BoundingBoxManager when improvedHitboxes is on).
 * TODO(T07): WorkbenchIngredient serializer registration (Forge CraftingHelper has no
 *  Fabric equivalent; the serializer itself is rewritten in T07).
 * TODO(T11): SimplePlanesHelper.init() and the compat mod flags wiring.
 */
public class FabricGunMod implements ModInitializer
{
    public static boolean debugging = false;
    public static boolean controllableLoaded = false;
    public static boolean curiosLoaded = false;
    public static boolean backpackedLoaded = false;
    public static boolean playerReviveLoaded = false;
    public static boolean sopLoaded = false;
    public static boolean travelersBackpackLoaded = false;
    public static boolean l2BackpackLoaded = false;
    public static boolean cmdCamLoaded = false;
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    @Override
    public void onInitialize()
    {
        ForgeConfigRegistry.INSTANCE.register(Reference.MOD_ID, ModConfig.Type.CLIENT, Config.clientSpec);
        ForgeConfigRegistry.INSTANCE.register(Reference.MOD_ID, ModConfig.Type.COMMON, Config.commonSpec);
        ForgeConfigRegistry.INSTANCE.register(Reference.MOD_ID, ModConfig.Type.SERVER, Config.serverSpec);

        // Static initializers of these classes perform the vanilla registry
        // registrations, in the same order as the baseline DeferredRegisters.
        forceInit(ModBlocks.class);
        forceInit(ModContainers.class);
        forceInit(ModEffects.class);
        forceInit(ModEnchantments.class);
        forceInit(ModEntities.class);
        forceInit(ModItems.class);
        forceInit(ModParticleTypes.class);
        forceInit(ModRecipeSerializers.class);
        forceInit(ModRecipeTypes.class);
        forceInit(ModSounds.class);
        forceInit(ModTileEntities.class);
        LOGGER.info("CGM registrations complete");

        PacketHandler.init();
        FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.AIMING);
        FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.RELOADING);
        FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.SHOOTING);
        FrameworkAPI.registerLoginData(new ResourceLocation(Reference.MOD_ID, "network_gun_manager"), NetworkGunManager.LoginData::new);
        FrameworkAPI.registerLoginData(new ResourceLocation(Reference.MOD_ID, "custom_gun_manager"), CustomGunManager.LoginData::new);
        ProjectileManager.getInstance().registerFactory(ModItems.GRENADE.get(), (worldIn, entity, weapon, item, modifiedGun) -> new GrenadeEntity(ModEntities.GRENADE.get(), worldIn, entity, weapon, item, modifiedGun));
        ProjectileManager.getInstance().registerFactory(ModItems.MISSILE.get(), (worldIn, entity, weapon, item, modifiedGun) -> new MissileEntity(ModEntities.MISSILE.get(), worldIn, entity, weapon, item, modifiedGun));

        DelayedTask.register();
        ReloadTracker.register();
        SpreadTracker.register();
        NetworkGunManager.register();
        CustomGunLoader.register();
        new BoundingBoxManager().register();

        // Fabric replacement for Forge IEntityAdditionalSpawnData: send extra spawn
        // data when a player starts tracking (also covers re-entering tracking range).
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if(entity instanceof ISpawnDataEntity)
            {
                PacketHandler.getPlayChannel().sendToPlayer(() -> player, S2CMessageSpawnData.create(entity));
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
        {
            // nothing to clean server-side; client clears its cache on disconnect
        });

        FabricLoader loader = FabricLoader.getInstance();
        cmdCamLoaded = loader.isModLoaded("cmdcam");
        backpackedLoaded = isCompatAdapterUsable(loader, "backpacked", 3,
                "Backpacked", "the backpack reload integration");
        travelersBackpackLoaded = isCompatAdapterUsable(loader, "travelersbackpack", 9,
                "Traveler's Backpack", "the worn-backpack reload integration");
        sopLoaded = isSophisticatedAdapterUsable(loader);
        // Presence is not adapter support. These Forge adapters remain isolated.
        for(String mod : new String[] { "controllable", "curios", "playerrevive",
                "l2backpack", "simpleplanes" })
        {
            if(loader.isModLoaded(mod))
                LOGGER.warn("CGM Fabric integration for {} is not enabled in this build", mod);
        }
    }

    /**
     * Sophisticated Backpacks needs its Core library, and its declared requirement
     * ({@code sophisticatedcore >=1.20.1-1.2.7.7 <1.20.4}) is a hard dependency that Fabric Loader
     * already enforces: a mismatched pair does not start at all. So the pairing is checked by the
     * loader rather than re-parsed here, and both versions are reported for the evidence log.
     */
    private static boolean isSophisticatedAdapterUsable(FabricLoader loader)
    {
        if(!loader.isModLoaded("sophisticatedbackpacks"))
        {
            return false;
        }
        if(!loader.isModLoaded("sophisticatedcore"))
        {
            LOGGER.error("Sophisticated Backpacks is present without Sophisticated Core; that pairing is invalid and the backpack reload integration stays disabled.");
            return false;
        }
        LOGGER.info("CGM Sophisticated Backpacks integration enabled (sophisticatedbackpacks {}, sophisticatedcore {})",
                versionOf(loader, "sophisticatedbackpacks"), versionOf(loader, "sophisticatedcore"));
        return true;
    }

    private static String versionOf(FabricLoader loader, String modId)
    {
        return loader.getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    /**
     * Enables a compiled-against adapter only when the installed mod is a version the adapter was
     * actually written for. The adapters reference their mod's types directly, so pairing an
     * adapter with an incompatible build would fail with {@code NoClassDefFoundError} at the first
     * reload. Reporting the mismatch is more useful than a silent no-op or a crash.
     *
     * @param minMajor lowest mod major version whose API the adapter was verified against
     */
    private static boolean isCompatAdapterUsable(FabricLoader loader, String modId, int minMajor,
                                                 String displayName, String feature)
    {
        if(!loader.isModLoaded(modId))
        {
            return false;
        }
        String version = loader.getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        int major = -1;
        try
        {
            major = Integer.parseInt(version.split("\\.")[0].replaceAll("[^0-9].*$", ""));
        }
        catch(NumberFormatException ignored) {}
        if(major < minMajor)
        {
            LOGGER.error("{} {} is not supported; CGM needs {}.0.0 or newer for {}. That integration stays disabled.", displayName, version, minMajor, feature);
            return false;
        }
        LOGGER.info("CGM {} integration enabled ({} {})", displayName, displayName, version);
        return true;
    }

    public static boolean isDebugging()
    {
        return false; // baseline used !FMLEnvironment.production; wire dev flag when needed
    }

    private static void forceInit(Class<?> clazz)
    {
        try
        {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Failed to initialize " + clazz.getName(), e);
        }
    }
}
