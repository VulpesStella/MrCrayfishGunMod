package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Author: MrCrayfish
 */
public class AttachmentItem extends Item implements IMeta
{
    public AttachmentItem(Properties properties)
    {
        super(properties);
    }

    /* Dirty hack to apply enchant effect to attachments if gun is enchanted */
    @Override
    public boolean isFoil(ItemStack stack)
    {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
        {
            ItemStack weapon = GunRenderingHandler.get().getRenderingWeapon();
            if(weapon != null)
            {
                return weapon.getItem().isFoil(weapon);
            }
        }
        return super.isFoil(stack);
    }
}
