package com.mrcrayfish.guns.event;

import com.mrcrayfish.guns.entity.ProjectileEntity;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fabric port: simple static callback event replacing the Forge EventBus event.
 *
 * <p>Fired when a projectile hits a block or entity. Canceling prevents the
 * projectile's default hit handling (contract preserved from Forge).</p>
 *
 * @author Ocelot
 */
public final class GunProjectileHitEvent
{
    @FunctionalInterface
    public interface Listener
    {
        void accept(GunProjectileHitEvent event);
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    public static void register(Listener listener)
    {
        LISTENERS.add(listener);
    }

    /**
     * Fires the event to all listeners.
     *
     * @return true if the hit handling was canceled
     */
    public static boolean fire(HitResult result, ProjectileEntity projectile)
    {
        GunProjectileHitEvent event = new GunProjectileHitEvent(result, projectile);
        for (Listener listener : LISTENERS)
        {
            listener.accept(event);
            if (event.isCanceled())
            {
                return true;
            }
        }
        return event.isCanceled();
    }

    private final HitResult result;
    private final ProjectileEntity projectile;
    private boolean canceled;

    private GunProjectileHitEvent(HitResult result, ProjectileEntity projectile)
    {
        this.result = result;
        this.projectile = projectile;
    }

    /**
     * @return The result of the entity's ray trace
     */
    public HitResult getRayTrace()
    {
        return this.result;
    }

    /**
     * @return The projectile that hit
     */
    public ProjectileEntity getProjectile()
    {
        return this.projectile;
    }

    public boolean isCanceled()
    {
        return this.canceled;
    }

    public void setCanceled(boolean canceled)
    {
        this.canceled = canceled;
    }
}
