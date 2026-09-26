package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.MessageDirection;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.client.network.ClientPlayHandler;
import com.mrcrayfish.guns.entity.ISpawnDataEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

/**
 * Fabric replacement for Forge's IEntityAdditionalSpawnData transport. Sent when a
 * player starts tracking an entity that implements {@link ISpawnDataEntity}. On the
 * client the payload is applied immediately if the entity already exists, otherwise
 * it is cached (bounded) until the vanilla spawn packet creates it, because
 * START_TRACKING can fire before the vanilla spawn packet on Fabric.
 */
public class S2CMessageSpawnData extends PlayMessage<S2CMessageSpawnData>
{
    private int entityId;
    private UUID entityUuid;
    private ResourceLocation dimension;
    private byte[] data;

    public S2CMessageSpawnData() {}

    private S2CMessageSpawnData(int entityId, UUID entityUuid, ResourceLocation dimension, byte[] data)
    {
        this.entityId = entityId;
        this.entityUuid = entityUuid;
        this.dimension = dimension;
        this.data = data;
    }

    /**
     * Serializes the entity's spawn data into a new message. Server side only.
     */
    public static S2CMessageSpawnData create(Entity entity)
    {
        ISpawnDataEntity spawnData = (ISpawnDataEntity) entity;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try
        {
            spawnData.writeSpawnData(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return new S2CMessageSpawnData(entity.getId(), entity.getUUID(), entity.level().dimension().location(), bytes);
        }
        finally { buf.release(); }
    }

    @Override
    public void encode(S2CMessageSpawnData message, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(message.entityId);
        buffer.writeUUID(message.entityUuid);
        buffer.writeResourceLocation(message.dimension);
        buffer.writeByteArray(message.data);
    }

    @Override
    public S2CMessageSpawnData decode(FriendlyByteBuf buffer)
    {
        return new S2CMessageSpawnData(buffer.readVarInt(), buffer.readUUID(), buffer.readResourceLocation(), buffer.readByteArray(1024 * 1024));
    }

    @Override
    public void handle(S2CMessageSpawnData message, MessageContext context)
    {
        context.execute(() ->
        {
            ClientPlayHandler.handleSpawnData(message);
        });
        context.setHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public byte[] getData()
    {
        return this.data;
    }

    public UUID getEntityUuid() { return this.entityUuid; }
    public ResourceLocation getDimension() { return this.dimension; }
}
