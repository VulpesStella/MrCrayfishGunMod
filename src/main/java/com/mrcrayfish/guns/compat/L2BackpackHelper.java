package com.mrcrayfish.guns.compat;

import com.mrcrayfish.guns.common.AmmoContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Fabric port stub: baseline consulted L2 Backpack/Library containers (and Curios
 * curios slots). The Fabric L2 counterpart API is verified in T11; the call site in
 * Gun is guarded by FabricGunMod.l2BackpackLoaded (false until then).
 */
public class L2BackpackHelper
{
    public static AmmoContext findAmmo(Player player, ResourceLocation id)
    {
        // TODO(T11): L2 Backpack/Library container lookup via the Fabric port.
        return AmmoContext.NONE;
    }
}
