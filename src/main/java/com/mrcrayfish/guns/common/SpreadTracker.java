package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
public class SpreadTracker
{
    private static final Map<Player, SpreadTracker> TRACKER_MAP = new WeakHashMap<>();

    private final Map<GunItem, Pair<MutableLong, MutableInt>> SPREAD_TRACKER_MAP = new HashMap<>();

    public void update(Player player, GunItem item)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.computeIfAbsent(item, gun -> Pair.of(new MutableLong(-1), new MutableInt()));
        MutableLong lastFire = entry.getLeft();
        if(lastFire.getValue() != -1)
        {
            MutableInt spreadCount = entry.getRight();
            long deltaTime = System.currentTimeMillis() - lastFire.getValue();
            if(deltaTime < Config.COMMON.projectileSpread.spreadThreshold.get())
            {
                if(spreadCount.getValue() < Config.COMMON.projectileSpread.maxCount.get())
                {
                    spreadCount.increment();

                    /* Increases the spread count quicker if the player is not aiming down sight */
                    if(spreadCount.getValue() < Config.COMMON.projectileSpread.maxCount.get() && !ModSyncedDataKeys.AIMING.getValue(player))
                    {
                        spreadCount.increment();
                    }
                }
            }
            else
            {
                spreadCount.setValue(0);
            }
        }
        lastFire.setValue(System.currentTimeMillis());
    }

    public float getSpread(GunItem item)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.get(item);
        if(entry != null)
        {
            return (float) entry.getRight().getValue() / (float) Config.COMMON.projectileSpread.maxCount.get();
        }
        return 0F;
    }

    public static SpreadTracker get(Player player)
    {
        return TRACKER_MAP.computeIfAbsent(player, player1 -> new SpreadTracker());
    }

    public static void register()
    {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            server.execute(() -> TRACKER_MAP.remove(player));
        });
    }
}
