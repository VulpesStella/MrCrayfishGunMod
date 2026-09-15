package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.client.network.ClientPlayHandler;
import com.mrcrayfish.guns.entity.ISpawnDataEntity;
import com.mrcrayfish.guns.network.message.S2CMessageSpawnData;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabric: EntityTrackingEvents.START_TRACKING fires before the vanilla spawn packet
 * reaches the client, so spawn-data payloads for entities that do not exist yet are
 * cached here and applied on {@link ClientEntityEvents#ENTITY_LOAD}. Entries are
 * bounded by count and age; the cache is cleared on disconnect.
 */
@Environment(EnvType.CLIENT)
public class SpawnDataCache
{
    private static final int MAX_SIZE = 256;
    private static final long LIFETIME_MS = 10_000L;

    private static final Map<UUID, Entry> PENDING = new ConcurrentHashMap<>();
    private static ClientLevel currentLevel;

    public static void handle(S2CMessageSpawnData message)
    {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        checkLevel(level);
        if(level == null || !level.dimension().location().equals(message.getDimension()))
            return;

        Entity entity = level.getEntity(message.getEntityId());
        if(entity instanceof ISpawnDataEntity && entity.getUUID().equals(message.getEntityUuid()))
        {
            apply(entity, message.getData());
        }
        else
        {
            if(PENDING.size() >= MAX_SIZE)
            {
                PENDING.entrySet().stream().min(java.util.Comparator.comparingLong(e -> e.getValue().time()))
                        .ifPresent(entry -> PENDING.remove(entry.getKey()));
            }
            PENDING.put(message.getEntityUuid(), new Entry(message.getData(), System.currentTimeMillis()));
        }
    }

    public static void register()
    {
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            checkLevel(world);
            Entry entry = PENDING.remove(entity.getUUID());
            if(entry != null && entity instanceof ISpawnDataEntity)
            {
                apply(entity, entry.data());
            }
        });
    }

    public static void tick()
    {
        checkLevel(Minecraft.getInstance().level);
        if(PENDING.isEmpty())
            return;
        long now = System.currentTimeMillis();
        Iterator<Entry> it = PENDING.values().iterator();
        while(it.hasNext())
        {
            if(now - it.next().time() > LIFETIME_MS)
            {
                it.remove();
            }
        }
    }

    public static void clear()
    {
        PENDING.clear();
        currentLevel = null;
    }

    private static void checkLevel(ClientLevel level)
    {
        if(currentLevel != level)
        {
            PENDING.clear();
            currentLevel = level;
        }
    }

    private static void apply(Entity entity, byte[] data)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        try { ((ISpawnDataEntity) entity).readSpawnData(buf); }
        finally { buf.release(); }
    }

    private record Entry(byte[] data, long time)
    {
    }
}
