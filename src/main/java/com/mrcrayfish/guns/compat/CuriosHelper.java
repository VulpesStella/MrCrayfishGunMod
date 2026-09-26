package com.mrcrayfish.guns.compat;

import com.mrcrayfish.guns.FabricGunMod;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

/**
 * Fabric port: Curios API is not on the compile classpath; access is deferred to
 * reflection and only executed when curiosLoaded is true (T11 verifies the Fabric
 * Curios counterpart). Baseline called CuriosApi directly.
 */
public class CuriosHelper
{
    private static Method getCuriosHelper;

    public static void runOnCurios(Player player, java.util.function.Consumer<Object> consumer)
    {
        if(!FabricGunMod.curiosLoaded)
            return;
        try
        {
            if(getCuriosHelper == null)
            {
                getCuriosHelper = Class.forName("top.theillusivec4.curios.api.CuriosApi").getMethod("getCuriosHelper");
            }
            Object helper = getCuriosHelper.invoke(null);
            consumer.accept(helper);
        }
        catch(ReflectiveOperationException | RuntimeException e)
        {
            FabricGunMod.LOGGER.warn("Curios lookup failed", e);
        }
    }
}
