package com.mrcrayfish.guns.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Fabric port: TargetBlock#updateRedstoneOutput is opened via cgm.accesswidener
 * (baseline used SRG reflection m_57391_).
 */
public class ReflectionUtil
{
    public static int updateTargetBlock(TargetBlock block, LevelAccessor level, BlockState state, BlockHitResult hitResult, Entity entity)
    {
        return TargetBlock.updateRedstoneOutput(level, state, hitResult, entity);
    }
}
