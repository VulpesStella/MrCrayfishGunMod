package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.client.Minecraft;

/**
 * Fabric port: Forge KeyConflictContext/Controllable IBindingContext have no Fabric
 * equivalent; the context is only consumed by CGM's own key handling.
 * TODO(T11): controller bindings evaluate this context through the Controllable hook.
 *
 * Author: MrCrayfish
 */
public enum GunConflictContext
{
    IN_GAME_HOLDING_WEAPON
    {
        public boolean isActive()
        {
            Minecraft mc = Minecraft.getInstance();
            return mc.screen == null && mc.player != null && mc.player.getMainHandItem().getItem() instanceof GunItem;
        }
    }
}
