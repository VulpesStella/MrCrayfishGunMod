package com.mrcrayfish.guns.client;

import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.mrcrayfish.guns.client.handler.AimingHandler;
import com.mrcrayfish.guns.client.handler.BulletTrailRenderingHandler;
import com.mrcrayfish.guns.client.handler.CrosshairHandler;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.client.handler.RecoilHandler;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.client.handler.ShootingHandler;
import com.mrcrayfish.guns.client.handler.SoundHandler;
import com.mrcrayfish.guns.client.render.gun.ModelOverrides;
import com.mrcrayfish.guns.client.CustomGunManager;
import com.mrcrayfish.guns.client.PackHandler;
import com.mrcrayfish.guns.client.ParticleFactoryRegistry;
import com.mrcrayfish.guns.client.SpawnDataCache;
import com.mrcrayfish.guns.item.attachment.impl.Attachment;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * Fabric client entrypoint.
 */
public class FabricClientSetup implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        FrameworkClientAPI.registerDataLoader(MetaLoader.getInstance());
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> Attachment.addInformation(stack, lines));
        SpawnDataCache.register();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> SpawnDataCache.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CustomGunManager.onClientDisconnect());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SpawnDataCache.clear());

        KeyBinds.register();
        PackHandler.register();
        ParticleFactoryRegistry.register();
        GunEntityRenderers.register();

        // ClientHandler.setup wires handlers, creative tab, screens, model loading,
        // input hooks and per-frame render hooks (baseline FMLClientSetup equivalent).
        ClientHandler.setup();

    }
}
