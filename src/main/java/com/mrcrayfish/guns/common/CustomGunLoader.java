package com.mrcrayfish.guns.common;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mrcrayfish.guns.FabricGunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.annotation.Validator;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class CustomGunLoader extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON_INSTANCE = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ResourceLocation.class, JsonDeserializers.RESOURCE_LOCATION);
        builder.registerTypeAdapter(ItemStack.class, JsonDeserializers.ITEM_STACK);
        builder.registerTypeAdapter(GripType.class, JsonDeserializers.GRIP_TYPE);
        return builder.create();
    });

    private static CustomGunLoader instance;

    private Map<ResourceLocation, CustomGun> customGunMap = new HashMap<>();

    public CustomGunLoader()
    {
        super(GSON_INSTANCE, "custom_guns");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler)
    {
        ImmutableMap.Builder<ResourceLocation, CustomGun> builder = ImmutableMap.builder();
        objects.forEach((resourceLocation, object) ->
        {
            try
            {
                CustomGun customGun = GSON_INSTANCE.fromJson(object, CustomGun.class);
                if(customGun != null && Validator.isValidObject(customGun))
                {
                    builder.put(resourceLocation, customGun);
                }
                else
                {
                    FabricGunMod.LOGGER.error("Couldn't load data file {} as it is missing or malformed", resourceLocation);
                }
            }
            catch(InvalidObjectException e)
            {
                FabricGunMod.LOGGER.error("Missing required properties for {}", resourceLocation);
                e.printStackTrace();
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }
        });
        this.customGunMap = builder.build();
    }

    /**
     * Writes all custom guns into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeCustomGuns(FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(this.customGunMap.size());
        this.customGunMap.forEach((id, gun) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(gun.serializeNBT());
        });
    }

    /**
     * Reads all registered guns from the provided packet buffer
     *
     * @param buffer a packet buffer get
     * @return a map of registered guns from the server
     */
    public static ImmutableMap<ResourceLocation, CustomGun> readCustomGuns(FriendlyByteBuf buffer)
    {
        int size = buffer.readVarInt();
        if(size > 0)
        {
            ImmutableMap.Builder<ResourceLocation, CustomGun> builder = ImmutableMap.builder();
            for(int i = 0; i < size; i++)
            {
                ResourceLocation id = buffer.readResourceLocation();
                CustomGun customGun = new CustomGun();
                customGun.deserializeNBT(buffer.readNbt());
                builder.put(id, customGun);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    /**
     * Fabric port: fresh CustomGunLoader per data reload, mirroring the baseline
     * AddReloadListenerEvent behavior.
     */
    public static void register()
    {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener()
        {
            @Override
            public ResourceLocation getFabricId()
            {
                return new ResourceLocation(Reference.MOD_ID, "custom_gun_loader");
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor)
            {
                CustomGunLoader customGunLoader = new CustomGunLoader();
                CustomGunLoader.instance = customGunLoader;
                return customGunLoader.reload(preparationBarrier, resourceManager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
            }
        });
    }

    @Nullable
    public static CustomGunLoader get()
    {
        return instance;
    }
}
