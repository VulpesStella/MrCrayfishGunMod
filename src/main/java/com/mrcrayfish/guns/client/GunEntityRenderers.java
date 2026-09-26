package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.client.render.entity.GrenadeRenderer;
import com.mrcrayfish.guns.client.render.entity.MissileRenderer;
import com.mrcrayfish.guns.client.render.entity.ProjectileRenderer;
import com.mrcrayfish.guns.client.render.entity.ThrowableGrenadeRenderer;
import com.mrcrayfish.guns.init.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Author: MrCrayfish
 */
public class GunEntityRenderers
{
    /**
     * Fabric port: was {@code @SubscribeEvent EntityRenderersEvent.RegisterRenderers} (mod bus).
     * Must be called during client mod init, before entity renderers are baked.
     */
    public static void register()
    {
        EntityRendererRegistry.register(ModEntities.PROJECTILE.get(), ProjectileRenderer::new);
        EntityRendererRegistry.register(ModEntities.GRENADE.get(), GrenadeRenderer::new);
        EntityRendererRegistry.register(ModEntities.MISSILE.get(), MissileRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWABLE_GRENADE.get(), ThrowableGrenadeRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWABLE_STUN_GRENADE.get(), ThrowableGrenadeRenderer::new);
    }
}
