package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.Reference;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple system to run synchronized delayed tasks. See {@link #runAfter(int, Runnable)} to add
 * a delayed task.
 * <p>
 * Fabric port: server reference is tracked via lifecycle events (Forge used
 * LogicalSidedProvider). Tasks run on END_SERVER_TICK, matching the baseline
 * Phase.END behavior. The server instance is cleared on stop so a restarted
 * server never reuses stale tasks.
 * <p>
 * Author: MrCrayfish
 */
public class DelayedTask
{
    private static MinecraftServer currentServer = null;
    public static List<Impl> tasks = new ArrayList<>();

    public static void register()
    {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> tasks.clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> tasks.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            tasks.clear();
            currentServer = null;
        });
        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerTickEvents.END_SERVER_TICK.register(DelayedTask::onServerTick);
    }

    private static void onServerTick(MinecraftServer server)
    {
        Iterator<Impl> it = tasks.iterator();
        while (it.hasNext())
        {
            Impl impl = it.next();
            if (impl.executionTick <= server.getTickCount())
            {
                impl.runnable.run();
                it.remove();
            }
        }
    }

    /**
     * Adds a new delayed task to the system.
     *
     * @param ticks the amount of ticks to delay the execution
     * @param run   a runnable get with the code to run
     */
    public static void runAfter(int ticks, Runnable run)
    {
        if (currentServer == null)
        {
            throw new IllegalStateException("Tried to add a delayed task without a running server");
        }
        if (!currentServer.isSameThread())
        {
            throw new IllegalStateException("Tried to add a delayed task off the main thread");
        }
        tasks.add(new Impl(currentServer.getTickCount() + ticks, run));
    }

    private static class Impl
    {
        private int executionTick;
        private Runnable runnable;

        private Impl(int executionTick, Runnable runnable)
        {
            this.executionTick = executionTick;
            this.runnable = runnable;
        }
    }
}
