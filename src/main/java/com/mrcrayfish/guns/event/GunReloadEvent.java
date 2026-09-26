package com.mrcrayfish.guns.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Fabric port: simple static callback event replacing the Forge EventBus.
 * Listener lists are CopyOnWriteArrayList so registration order is stable and
 * concurrent registration is safe.
 *
 * <p>Fired when a player shoots a gun.</p>
 *
 * @author Ocelot
 */
public final class GunReloadEvent
{
    @FunctionalInterface
    public interface PreListener
    {
        void accept(Pre event);
    }

    @FunctionalInterface
    public interface PostListener
    {
        void accept(Post event);
    }

    private static final List<PreListener> PRE_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<PostListener> POST_LISTENERS = new CopyOnWriteArrayList<>();

    public static void registerPre(PreListener listener)
    {
        PRE_LISTENERS.add(listener);
    }

    public static void registerPost(PostListener listener)
    {
        POST_LISTENERS.add(listener);
    }

    /**
     * Fires the Pre event to all listeners.
     *
     * @return true if the fire was canceled by any listener
     */
    public static boolean firePre(Player player, ItemStack stack)
    {
        Pre event = new Pre(player, stack);
        for (PreListener listener : PRE_LISTENERS)
        {
            listener.accept(event);
            if (event.isCanceled())
            {
                return true;
            }
        }
        return event.isCanceled();
    }

    public static void firePost(Player player, ItemStack stack)
    {
        Post event = new Post(player, stack);
        for (PostListener listener : POST_LISTENERS)
        {
            listener.accept(event);
        }
    }

    /**
     * <p>Fired when a player is about to reloads a gun. Canceling this event prevents
     * ammo consumption and projectile creation (contract preserved from Forge).</p>
     *
     * @author Ocelot
     */
    public static class Pre extends GunReloadEventData
    {
        private boolean canceled;

        public Pre(Player player, ItemStack stack)
        {
            super(player, stack);
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

    /**
     * <p>Fired after a player has started reloading a gun.</p>
     *
     * @author Ocelot
     */
    public static class Post extends GunReloadEventData
    {
        public Post(Player player, ItemStack stack)
        {
            super(player, stack);
        }
    }

    private abstract static class GunReloadEventData
    {
        private final Player player;
        private final ItemStack stack;

        protected GunReloadEventData(Player player, ItemStack stack)
        {
            this.player = player;
            this.stack = stack;
        }

        public Player getPlayer()
        {
            return this.player;
        }

        /**
         * @return The stack the player was holding when firing the gun
         */
        public ItemStack getStack()
        {
            return this.stack;
        }

        /**
         * @return Whether or not this event was fired on the client side
         */
        public boolean isClient()
        {
            return this.player.getCommandSenderWorld().isClientSide();
        }
    }
}
