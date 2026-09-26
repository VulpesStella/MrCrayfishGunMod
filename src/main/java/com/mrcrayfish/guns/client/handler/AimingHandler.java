package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.FabricGunMod;
import com.mrcrayfish.guns.client.KeyBinds;
import com.mrcrayfish.guns.client.util.PropertyHelper;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.compat.PlayerReviveHelper;
import com.mrcrayfish.guns.debug.Debug;
import com.mrcrayfish.guns.init.ModBlocks;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageAim;
import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
public class AimingHandler
{
    private static AimingHandler instance;

    public static AimingHandler get()
    {
        if(instance == null)
        {
            instance = new AimingHandler();
        }
        return instance;
    }

    private static final double MAX_AIM_PROGRESS = 5;
    private final AimTracker localTracker = new AimTracker();
    private final Map<Player, AimTracker> aimingMap = new WeakHashMap<>();
    private double normalisedAdsProgress;
    private boolean aiming = false;

    private AimingHandler() {}

    /**
     * Fabric port: registers the Fabric event hooks that replaced the Forge
     * {@code @SubscribeEvent} handlers (Forge: {@code MinecraftForge.EVENT_BUS.register(this)}).
     */
    public static void register()
    {
        // Forge: TickEvent.PlayerTickEvent(Phase.START) fired for every ticked player.
        // Fabric has no per-player client tick event, so every player of the client world
        // is processed once per client tick (the Forge handler only ever did meaningful
        // work for client-side players; server-side trackers were never read).
        ClientTickEvents.START_CLIENT_TICK.register(mc ->
        {
            if(mc.level != null)
            {
                for(Player player : mc.level.players())
                {
                    AimingHandler.get().onPlayerTick(player);
                }
            }
        });
        // Forge: TickEvent.ClientTickEvent(Phase.START)
        ClientTickEvents.START_CLIENT_TICK.register(mc -> AimingHandler.get().onClientTick());
        // Forge: ClientPlayerNetworkEvent.LoggingOut -> Fabric ClientPlayConnectionEvents.DISCONNECT
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AimingHandler.get().aimingMap.clear());
        // Forge: RenderGuiOverlayEvent(receiveCanceled = true), used purely to refresh the
        // normalized ADS progress each frame. Fabric's HudRenderCallback fires once per frame
        // during HUD rendering, which is equivalent for this bookkeeping update.
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> AimingHandler.get().normalisedAdsProgress = AimingHandler.get().localTracker.getNormalProgress(tickDelta));
    }

    void onPlayerTick(Player player)
    {
        AimTracker tracker = getAimTracker(player);
        if(tracker != null)
        {
            tracker.handleAiming(player, player.getItemInHand(InteractionHand.MAIN_HAND));
            if(!tracker.isAiming())
            {
                this.aimingMap.remove(player);
            }
        }
    }

    @Nullable
    private AimTracker getAimTracker(Player player)
    {
        if(ModSyncedDataKeys.AIMING.getValue(player) && !this.aimingMap.containsKey(player))
        {
            this.aimingMap.put(player, new AimTracker());
        }
        return this.aimingMap.get(player);
    }

    public float getAimProgress(Player player, float partialTicks)
    {
        if(player.isLocalPlayer())
        {
            return (float) this.localTracker.getNormalProgress(partialTicks);
        }

        AimTracker tracker = this.getAimTracker(player);
        if(tracker != null)
        {
            return (float) tracker.getNormalProgress(partialTicks);
        }
        return 0F;
    }

    void onClientTick()
    {
        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        if(this.isAiming())
        {
            if(!this.aiming)
            {
                ModSyncedDataKeys.AIMING.setValue(player, true);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(true));
                this.aiming = true;
            }
        }
        else if(this.aiming)
        {
            ModSyncedDataKeys.AIMING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
            this.aiming = false;
        }

        this.localTracker.handleAiming(player, player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    /**
     * Forge port note: was {@code @SubscribeEvent ViewportEvent.ComputeFov} and modified the FOV
     * while aiming down a scoped sight. Fabric 1.20.1 has no FOV modification event, so the
     * original handler body is kept as a plain method that applies the modifier and returns the
     * resulting FOV instead of writing it back to the event.
     *
     * TODO(T06): needs a narrow Mixin on GameRenderer#getFov to call this (see EVENT-INVENTORY.md).
     *
     * @param fov the current FOV
     * @param usedConfiguredFov mirrors Forge's usedConfiguredFov(), false when the FOV was
     *                          already altered (e.g. flying/spying) and must not be touched
     * @return the FOV to use
     */
    public float onFovUpdate(float fov, boolean usedConfiguredFov)
    {
        if(!usedConfiguredFov)
            return fov;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.player.getMainHandItem().isEmpty() || mc.options.getCameraType() != CameraType.FIRST_PERSON)
            return fov;

        ItemStack heldItem = mc.player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem gunItem))
            return fov;

        if(AimingHandler.get().getNormalisedAdsProgress() == 0)
            return fov;

        if(ModSyncedDataKeys.RELOADING.getValue(mc.player))
            return fov;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(modifiedGun.getModules().getZoom() == null)
            return fov;

        double time = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getFovCurve().apply(this.normalisedAdsProgress);
        float modifier = Gun.getFovModifier(heldItem, modifiedGun);
        modifier = (1.0F - modifier) * (float) time;
        return fov - fov * modifier;
    }

    public boolean isZooming()
    {
        return this.aiming;
    }

    public boolean isAiming()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return false;

        if(mc.player.isSpectator())
            return false;

        if(Debug.isForceAim())
            return true;

        if(mc.screen != null)
            return false;

        if(PlayerReviveHelper.isBleeding(mc.player))
            return false;

        ItemStack heldItem = mc.player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem))
            return false;

        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        if(!gun.canAimDownSight())
            return false;

        if(mc.player.getOffhandItem().getItem() == Items.SHIELD && gun.getGeneral().getGripType() == GripType.ONE_HANDED)
            return false;

        if(!this.localTracker.isAiming() && this.isLookingAtInteractableBlock())
            return false;

        if(ModSyncedDataKeys.RELOADING.getValue(mc.player))
            return false;

        boolean zooming = KeyBinds.getAimMapping().isDown();
        if(FabricGunMod.controllableLoaded)
        {
            // TODO(T11): Controllable hook - the Fabric Controllable counterpart is
            // verified in T11; the flag is false on Fabric so this branch never runs.
            zooming |= false;
        }

        return zooming;
    }

    public boolean isLookingAtInteractableBlock()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.hitResult != null && mc.level != null)
        {
            if(mc.hitResult instanceof BlockHitResult result)
            {
                BlockState state = mc.level.getBlockState(result.getBlockPos());
                Block block = state.getBlock();
                // Forge should add a tag for intractable blocks so modders can know which blocks can be interacted with :)
                return block instanceof EntityBlock || block == Blocks.CRAFTING_TABLE || block == ModBlocks.WORKBENCH.get() || state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS) || block instanceof ChestBlock || state.is(BlockTags.FENCE_GATES);
            }
            else if(mc.hitResult instanceof EntityHitResult result)
            {
                return result.getEntity() instanceof ItemFrame;
            }
        }
        return false;
    }

    public double getNormalisedAdsProgress()
    {
        return this.normalisedAdsProgress;
    }

    public class AimTracker
    {
        private double currentAim;
        private double previousAim;

        private void handleAiming(Player player, ItemStack heldItem)
        {
            this.previousAim = this.currentAim;
            if(ModSyncedDataKeys.AIMING.getValue(player) || (player.isLocalPlayer() && AimingHandler.this.isAiming()))
            {
                if(this.currentAim < MAX_AIM_PROGRESS)
                {
                    double speed = GunEnchantmentHelper.getAimDownSightSpeed(heldItem);
                    speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, speed);
                    this.currentAim += speed;
                    if(this.currentAim > MAX_AIM_PROGRESS)
                    {
                        this.currentAim = (int) MAX_AIM_PROGRESS;
                    }
                }
            }
            else
            {
                if(this.currentAim > 0)
                {
                    double speed = GunEnchantmentHelper.getAimDownSightSpeed(heldItem);
                    speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, speed);
                    this.currentAim -= speed;
                    if(this.currentAim < 0)
                    {
                        this.currentAim = 0;
                    }
                }
            }
        }

        public boolean isAiming()
        {
            return this.currentAim != 0 || this.previousAim != 0;
        }

        public double getNormalProgress(float partialTicks)
        {
            return Mth.clamp((this.previousAim + (this.currentAim - this.previousAim) * partialTicks) / MAX_AIM_PROGRESS, 0.0, 1.0);
        }
    }
}
