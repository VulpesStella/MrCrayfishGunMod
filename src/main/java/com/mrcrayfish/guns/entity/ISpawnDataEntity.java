package com.mrcrayfish.guns.entity;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Fabric replacement for Forge's IEntityAdditionalSpawnData. Entities implementing
 * this interface have their extra spawn data sent in a dedicated message when a
 * player starts tracking them (EntityTrackingEvents.START_TRACKING); the client
 * caches the payload if the vanilla spawn packet has not arrived yet.
 *
 * Author: MrCrayfish (baseline writeSpawnData/readSpawnData preserved verbatim)
 */
public interface ISpawnDataEntity
{
    void writeSpawnData(FriendlyByteBuf buffer);

    void readSpawnData(FriendlyByteBuf buffer);
}
