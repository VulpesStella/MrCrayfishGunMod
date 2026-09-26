package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public enum SpecialModels
{
    ASSAULT_RIFLE("gun/assault_rifle"),
    BAZOOKA("gun/bazooka"),
    HEAVY_RIFLE("gun/heavy_rifle"),
    MACHINE_PISTOL("gun/machine_pistol"),
    PISTOL("gun/pistol"),
    RIFLE("gun/rifle"),
    SHOTGUN("gun/shotgun"),
    FLAME("flame"),
    MINI_GUN_BASE("mini_gun_base"),
    MINI_GUN_BARRELS("mini_gun_barrels"),
    GRENADE_LAUNCHER_BASE("grenade_launcher_base"),
    GRENADE_LAUNCHER_CYLINDER("grenade_launcher_cylinder");

    /**
     * The location of an item model in the [MOD_ID]/models/special/[NAME] folder
     */
    private final ResourceLocation modelLocation;

    /**
     * Cached model
     */
    private BakedModel cachedModel;

    /**
     * Sets the model's location
     *
     * @param modelName name of the model file
     */
    SpecialModels(String modelName)
    {
        this.modelLocation = new ResourceLocation(Reference.MOD_ID, "special/" + modelName);
    }

    /**
     * Gets the model
     *
     * @return isolated model
     */
    public BakedModel getModel()
    {
        if(this.cachedModel == null)
        {
            this.cachedModel = Minecraft.getInstance().getModelManager().getModel(this.modelLocation);
        }
        return this.cachedModel;
    }

    /**
     * Fabric port of the Forge ModelEvent handlers: extra models are registered through a
     * ModelLoadingPlugin (see FabricClientSetup) and the cached BakedModel is invalidated
     * after every bake so resource reloads rebuild it on demand.
     */
    public static void registerAdditional(net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context context)
    {
        for(SpecialModels model : values())
        {
            context.addModels(model.modelLocation);
        }
    }

    public static void onBake()
    {
        for(SpecialModels model : values())
        {
            model.cachedModel = null;
        }
    }
}
