package com.mrcrayfish.guns.client.handler;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.render.crosshair.Crosshair;
import com.mrcrayfish.guns.client.render.crosshair.TechCrosshair;
import com.mrcrayfish.guns.client.render.crosshair.TexturedCrosshair;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.item.GunItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class CrosshairHandler
{
    private static CrosshairHandler instance;

    public static CrosshairHandler get()
    {
        if(instance == null)
        {
            instance = new CrosshairHandler();
        }
        return instance;
    }

    private final Map<ResourceLocation, Crosshair> idToCrosshair = new HashMap<>();
    private final List<Crosshair> registeredCrosshairs = new ArrayList<>();
    private Crosshair currentCrosshair = null;

    private CrosshairHandler()
    {
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "better_default")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "circle")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "filled_circle"), false));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "square")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "round")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "arrow")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "dot")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "box")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "hit_marker")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "line")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "t")));
        this.register(new TexturedCrosshair(new ResourceLocation(Reference.MOD_ID, "smiley")));
        this.register(new TechCrosshair());
    }

    /**
     * Fabric port: registers the Fabric event hooks that replaced the Forge
     * {@code @SubscribeEvent} handlers (Forge: {@code MinecraftForge.EVENT_BUS.register(this)}).
     */
    public static void register()
    {
        // Forge: TickEvent.ClientTickEvent(Phase.END)
        ClientTickEvents.END_CLIENT_TICK.register(mc -> CrosshairHandler.get().onClientTick());
        // Forge: GunFireEvent.Post (custom event, now a Fabric callback, see event/GunFireEvent)
        GunFireEvent.registerPost(event -> CrosshairHandler.get().onGunFired(event));
        // Forge: ModConfigEvent.Reloading on the mod bus (was wired from GunMod). Forge Config
        // API Port exposes the equivalent per-mod reloading event. The event is already scoped
        // to this mod's configs; the original CLIENT-type check is not needed since re-reading
        // the client display config is idempotent (CGM's crosshair setting only exists there).
        ModConfigEvents.reloading(Reference.MOD_ID).register(config -> CrosshairHandler.onConfigReload());
        /* NOTE (T06): the Forge handler also subscribed RenderGuiOverlayEvent.Pre to cancel the
         * vanilla crosshair overlay and draw the custom one. HudRenderCallback has no
         * cancellation semantics, so nothing is drawn from Fabric events yet. See
         * onRenderOverlay(GuiGraphics, float) below and the TODO there. */
    }

    /**
     * Registers a new crosshair. If the crosshair has already been registered, it will be ignored.
     */
    public void register(Crosshair crosshair)
    {
        if(!this.idToCrosshair.containsKey(crosshair.getLocation()))
        {
            this.idToCrosshair.put(crosshair.getLocation(), crosshair);
            this.registeredCrosshairs.add(crosshair);
        }
    }

    /**
     * Sets the crosshair using the given id. The crosshair with the associated id must be registered
     * or the default crosshair will be used.
     *
     * @param id the id of the crosshair
     */
    public void setCrosshair(ResourceLocation id)
    {
        this.currentCrosshair = this.idToCrosshair.getOrDefault(id, Crosshair.DEFAULT);
    }

    /**
     * Gets the current crosshair
     */
    @Nullable
    public Crosshair getCurrentCrosshair()
    {
        if(this.currentCrosshair == null && this.registeredCrosshairs.size() > 0)
        {
            ResourceLocation id = ResourceLocation.tryParse(Config.CLIENT.display.crosshair.get());
            this.currentCrosshair = id != null ? this.idToCrosshair.getOrDefault(id, Crosshair.DEFAULT) : Crosshair.DEFAULT;
        }
        return this.currentCrosshair;
    }

    /**
     * Gets a list of registered crosshairs. Please note that this list is immutable.
     */
    public List<Crosshair> getRegisteredCrosshairs()
    {
        return ImmutableList.copyOf(this.registeredCrosshairs);
    }

    /**
     * Forge port note: was {@code @SubscribeEvent RenderGuiOverlayEvent.Pre}. In Forge it
     * canceled the vanilla crosshair overlay ({@code VanillaGuiOverlay.CROSSHAIR}) when aiming
     * or when the held gun provides a custom crosshair, then drew the custom crosshair.
     * Fabric's HudRenderCallback can not cancel vanilla HUD overlays, so this is kept as a
     * plain method with the original body (cancellations turned into early returns).
     *
     * TODO(T06): hide the vanilla crosshair with a narrow Mixin (see EVENT-INVENTORY.md), then
     * call this from a HudRenderCallback to draw the custom crosshair.
     *
     * @param graphics the GuiGraphics of the current HUD render
     * @param partialTick the current tick delta
     */
    /**
     * Fabric port: Forge canceled RenderGuiOverlayEvent.Pre (hiding the vanilla
     * crosshair) whenever a gun was held or while aiming; GuiMixin#renderCrosshair
     * consults this to skip the vanilla draw, and HudRenderCallback draws the custom one.
     */
    public boolean shouldHideVanillaCrosshair()
    {
        if(AimingHandler.get().getNormalisedAdsProgress() > 0.5)
        {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return false;
        return mc.player.getMainHandItem().getItem() instanceof GunItem;
    }

    public void onRenderOverlay(GuiGraphics graphics, float partialTick)
    {
        Crosshair crosshair = this.getCurrentCrosshair();
        if(!this.shouldHideVanillaCrosshair())
        {
            return;
        }

        if(crosshair == null || crosshair.isDefault())
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        if(!mc.options.getCameraType().isFirstPerson())
            return;

        if(mc.player.getUseItem().getItem() == Items.SHIELD)
            return;

        PoseStack stack = graphics.pose();
        stack.pushPose();
        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        int scaledHeight = mc.getWindow().getGuiScaledHeight();
        crosshair.render(mc, stack, scaledWidth, scaledHeight, partialTick);
        stack.popPose();
    }

    void onClientTick()
    {
        Crosshair crosshair = this.getCurrentCrosshair();
        if(crosshair == null || crosshair.isDefault())
            return;

        crosshair.tick();
    }

    void onGunFired(GunFireEvent.Post event)
    {
        Crosshair crosshair = this.getCurrentCrosshair();
        if(crosshair == null || crosshair.isDefault())
            return;

        crosshair.onGunFired();
    }

    public static void registerHud()
    {
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((graphics, tickDelta) ->
                CrosshairHandler.get().onRenderOverlay(graphics, tickDelta));
    }

    /**
     * Updates the crosshair if the config is reloaded. Fabric port note: the Forge
     * {@code ModConfig.Type.CLIENT} and mod id checks are handled by the
     * {@link ModConfigEvents#reloading} registration (mod scoped) in {@link #register()}.
     */
    public static void onConfigReload()
    {
        ResourceLocation id = ResourceLocation.tryParse(Config.CLIENT.display.crosshair.get());
        if(id != null)
        {
            CrosshairHandler.get().setCrosshair(id);
        }
    }
}
