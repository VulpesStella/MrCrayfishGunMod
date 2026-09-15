package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.Reference;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric port of the Forge {@code AddPackFindersEvent} handler that registered the built-in
 * "feature/cgm_pbr_textures" resource pack from {@code packs/cgm_pbr} inside the cgm mod jar.
 *
 * <p>Fabric's resource loader registers built-in packs against the mod container: the pack id is
 * {@code cgm:cgm_pbr} and the pack content is read from the {@code packs/cgm_pbr} sub path of the
 * cgm mod root (resolved via {@link net.fabricmc.loader.api.ModContainer#getRootPaths()}).
 * {@code hidden = false} maps to {@link ResourcePackActivationType#NORMAL} (disabled by default),
 * matching the baseline pack creation.</p>
 *
 * <p>Note: Fabric derives the pack display name from the translation key {@code pack.cgm.cgm_pbr}.
 * The baseline used {@code pack.cgm.pbr.title}; add the new key to the lang files (or switch the
 * key) when resources are next touched, otherwise the raw key shows in the pack list.</p>
 */
public class PackHandler
{
    /**
     * Must be called during client mod init, before the resource pack repository is scanned.
     */
    public static void register()
    {
        FabricLoader.getInstance().getModContainer(Reference.MOD_ID).ifPresent(container ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                        new ResourceLocation(Reference.MOD_ID, "cgm_pbr"),
                        "packs/cgm_pbr",
                        container,
                        false));
    }
}
