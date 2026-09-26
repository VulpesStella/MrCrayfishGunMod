package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.blockentity.WorkbenchBlockEntity;
import com.mrcrayfish.guns.common.container.AttachmentContainer;
import com.mrcrayfish.guns.common.container.WorkbenchContainer;
import com.mrcrayfish.guns.util.RegistryObject;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Author: MrCrayfish
 */
public class ModContainers
{
    public static final RegistryObject<MenuType<WorkbenchContainer>> WORKBENCH = register("workbench", new ExtendedScreenHandlerType<>((windowId, playerInventory, data) -> {
        WorkbenchBlockEntity workstation = (WorkbenchBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos());
        return new WorkbenchContainer(windowId, playerInventory, workstation);
    }));

    public static final RegistryObject<MenuType<AttachmentContainer>> ATTACHMENTS = register("attachments", new MenuType<>(AttachmentContainer::new, FeatureFlags.DEFAULT_FLAGS));

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String id, MenuType<T> type)
    {
        return RegistryObject.of(Registry.register(BuiltInRegistries.MENU, new ResourceLocation(Reference.MOD_ID, id), type));
    }
}
