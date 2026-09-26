package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.FabricGunMod;
import com.mrcrayfish.guns.client.handler.*;
import com.mrcrayfish.guns.client.render.gun.ModelOverrides;
import com.mrcrayfish.guns.client.render.gun.model.GrenadeLauncherModel;
import com.mrcrayfish.guns.client.render.gun.model.MiniGunModel;
import com.mrcrayfish.guns.client.render.gun.model.SimpleModel;
import com.mrcrayfish.guns.client.screen.AttachmentScreen;
import com.mrcrayfish.guns.client.screen.WorkbenchScreen;
import com.mrcrayfish.guns.client.util.PropertyHelper;
import com.mrcrayfish.guns.debug.IEditorMenu;
import com.mrcrayfish.guns.debug.client.screen.EditorScreen;
import com.mrcrayfish.guns.enchantment.GunEnchantment;
import com.mrcrayfish.guns.event.GunFireEvent;
import net.minecraft.core.Registry;
import com.mrcrayfish.guns.init.ModBlocks;
import com.mrcrayfish.guns.init.ModContainers;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.IColored;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.color.item.ItemColor;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import com.mrcrayfish.guns.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ItemLike;
import com.mrcrayfish.guns.util.RegistryObject;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import org.lwjgl.glfw.GLFW;

import static com.mrcrayfish.guns.Reference.MOD_ID;

/**
 * Author: MrCrayfish
 */
@Environment(EnvType.CLIENT)
public class ClientHandler {

    public static void setup() {
        GunFireEvent.registerPost(event -> RecoilHandler.get().onGunFire(event));
        GunFireEvent.registerPost(event -> GunRenderingHandler.get().onGunFire(event));
        // NOTE: CrosshairHandler registers its own GunFireEvent listener (register()).
        AimingHandler.get().register();
        BulletTrailRenderingHandler.get().register();
        CrosshairHandler.get().register();
        CrosshairHandler.registerHud();
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((graphics, partialTick) -> {
            Minecraft mc = Minecraft.getInstance();
            if(mc.player == null) return;
            var effect = mc.player.getEffect(com.mrcrayfish.guns.init.ModEffects.BLINDED.get());
            if(effect == null) return;
            float percent = Math.min(effect.getDuration() / (float) Math.max(1, com.mrcrayfish.guns.Config.SERVER.alphaFadeThreshold.get()), 1F);
            int alpha = Math.max(0, Math.min(255, Math.round(percent * com.mrcrayfish.guns.Config.SERVER.alphaOverlay.get())));
            graphics.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), (alpha << 24) | 0xFFFFFF);
        });
        GunRenderingHandler.get().register();
        GunRenderingHandler.get().registerWorldRender();
        RecoilHandler.get().register();
        ReloadHandler.get().register();
        ShootingHandler.get().register();
        SoundHandler.get().register();
        ModelOverrides.register();

        /* Only register controller events if Controllable is loaded otherwise it will crash */
        // TODO(T11): Controllable ControllerHandler/GunButtonBindings wiring (Fabric
        // Controllable counterpart is verified in T11; the adapter classes are isolated
        // from the core compile until then).

        setupRenderLayers();
        registerColors();
        registerModelOverrides();
        registerScreenFactories();
        registerInputHandlers();
        registerReloadListener();
        registerModelLoading();
        registerCreativeTab();
        GunItemStackRenderer gunRenderer = new GunItemStackRenderer();
        for (RegistryObject<? extends ItemLike> item : ModItems.ALL_ITEMS) {
            if (item.get() instanceof GunItem gun) {
                net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.INSTANCE.register(gun,
                        gunRenderer::renderByItem);
            }
        }
    }

    private static void setupRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WORKBENCH.get(), RenderType.cutout());
    }

    private static void registerColors() {
        ItemColor color = (stack, index) -> {
            if (!IColored.isDyeable(stack)) {
                return -1;
            }
            if (index == 0 && stack.hasTag() && stack.getTag().contains("Color", Tag.TAG_INT)) {
                return stack.getTag().getInt("Color");
            }
            if (index == 0 && stack.getItem() instanceof IAttachment) {
                ItemStack renderingWeapon = GunRenderingHandler.get().getRenderingWeapon();
                if (renderingWeapon != null) {
                    ItemColor renderingColor = ColorProviderRegistry.ITEM.get(renderingWeapon.getItem());
                    return renderingColor != null ? renderingColor.getColor(renderingWeapon, index) : -1;
                }
            }
            if (index == 2) // Reticle colour
            {
                return PropertyHelper.getReticleColor(stack);
            }
            return -1;
        };
        // Fabric port: Forge patched Minecraft.getItemColors(); use ColorProviderRegistry.
        java.util.List<ItemLike> coloredItems = new java.util.ArrayList<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof IColored) {
                coloredItems.add(item);
            }
        });
        ColorProviderRegistry.ITEM.register(color, coloredItems.toArray(new ItemLike[0]));
    }

    private static void registerModelOverrides() {
        /* Weapons */
        ModelOverrides.register(ModItems.ASSAULT_RIFLE.get(), new SimpleModel(SpecialModels.ASSAULT_RIFLE::getModel));
        ModelOverrides.register(ModItems.BAZOOKA.get(), new SimpleModel(SpecialModels.BAZOOKA::getModel));
        ModelOverrides.register(ModItems.GRENADE_LAUNCHER.get(), new GrenadeLauncherModel());
        ModelOverrides.register(ModItems.HEAVY_RIFLE.get(), new SimpleModel(SpecialModels.HEAVY_RIFLE::getModel));
        ModelOverrides.register(ModItems.MACHINE_PISTOL.get(), new SimpleModel(SpecialModels.MACHINE_PISTOL::getModel));
        ModelOverrides.register(ModItems.MINI_GUN.get(), new MiniGunModel());
        ModelOverrides.register(ModItems.PISTOL.get(), new SimpleModel(SpecialModels.PISTOL::getModel));
        ModelOverrides.register(ModItems.RIFLE.get(), new SimpleModel(SpecialModels.RIFLE::getModel));
        ModelOverrides.register(ModItems.SHOTGUN.get(), new SimpleModel(SpecialModels.SHOTGUN::getModel));
    }

    private static void registerScreenFactories() {
        MenuScreens.register(ModContainers.WORKBENCH.get(), WorkbenchScreen::new);
        MenuScreens.register(ModContainers.ATTACHMENTS.get(), AttachmentScreen::new);
    }

    // Fabric port of ScreenEvent.Init.Post: the baseline reflection block only read the
    // options list (the actual additions were commented out upstream), so no access
    // widener entry is needed for MouseSettingsScreen.
    private static void onScreenInit(Minecraft mc, Screen screen, java.util.List<net.minecraft.client.gui.components.events.GuiEventListener> listeners) {
        if (screen instanceof MouseSettingsScreen mouseSettings) {
            //list.addBig(OptionInstance.createBoolean("t", true));
            //list.addSmall(GunOptions.ADS_SENSITIVITY, GunOptions.CROSSHAIR);
        }
    }

    public static void registerInputHandlers() {
        ScreenEvents.AFTER_INIT.register((mc, screen, scaledWidth, scaledHeight) -> onScreenInit(mc, screen, null));

        // Fabric port of InputEvent.Key handling: consumeClick() yields one true per press.
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (KeyBinds.KEY_ATTACHMENTS.consumeClick()) {
                if (mc.player != null && mc.screen == null && mc.isWindowActive()) {
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
                }
            }
        });
    }


    public static void registerReloadListener() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId()
            {
                return new ResourceLocation(Reference.MOD_ID, "property_helper");
            }

            @Override
            public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager resourceManager)
            {
                PropertyHelper.resetCache();
            }
        });
    }

    public static void registerModelLoading() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.addModels(new ResourceLocation(MOD_ID, "special/test"));
            SpecialModels.registerAdditional(pluginContext);
            pluginContext.modifyModelAfterBake().register((bakedModel, context) -> {
                SpecialModels.onBake();
                return bakedModel;
            });
        });
    }

    public static void registerCreativeTab() {
        CreativeModeTab tab = net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + MOD_ID))
            .icon(() -> {
                ItemStack stack = new ItemStack(ModItems.PISTOL.get());
                stack.getOrCreateTag().putBoolean("IgnoreAmmo", true);
                return stack;
            })
            .displayItems((flags, output) ->
            {
                for (RegistryObject<? extends ItemLike> registryObject : ModItems.ALL_ITEMS) {
                    if (registryObject.get() instanceof GunItem item) {
                        ItemStack stack = new ItemStack(item);
                        stack.getOrCreateTag().putInt("AmmoCount", item.getGun().getGeneral().getMaxAmmo());
                        output.accept(stack);
                        continue;
                    }
                    output.accept(registryObject.get());
                }
                CustomGunManager.fill(output);
                for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
                    if (enchantment instanceof GunEnchantment) {
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                    }
                }
            })
            .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(MOD_ID, "creative_tab"), tab);
    }

    public static Screen createEditorScreen(IEditorMenu menu) {
        return new EditorScreen(Minecraft.getInstance().screen, menu);
    }

    /* Uncomment for debugging headshot hit boxes */

    /*@SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onRenderLiving(RenderLivingEvent.Post event)
    {
        LivingEntity entity = event.getEntity();
        IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());
        if(headshotBox != null)
        {
            AxisAlignedBB box = headshotBox.getHeadshotBox(entity);
            if(box != null)
            {
                WorldRenderer.drawBoundingBox(event.getMatrixStack(), event.getBuffers().getBuffer(RenderType.getLines()), box, 1.0F, 1.0F, 0.0F, 1.0F);

                AxisAlignedBB boundingBox = entity.getBoundingBox().offset(entity.getPositionVec().inverse());
                boundingBox = boundingBox.grow(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get());
                WorldRenderer.drawBoundingBox(event.getMatrixStack(), event.getBuffers().getBuffer(RenderType.getLines()), boundingBox, 0.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }*/
}
