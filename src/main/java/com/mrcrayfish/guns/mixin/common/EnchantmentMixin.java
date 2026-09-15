package com.mrcrayfish.guns.mixin.common;

import com.mrcrayfish.guns.item.attachment.IAttachment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin
{
    @Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
    private void cgm$allowAttachmentBinding(ItemStack stack, CallbackInfoReturnable<Boolean> cir)
    {
        if((Object) this == Enchantments.BINDING_CURSE && stack.getItem() instanceof IAttachment)
            cir.setReturnValue(true);
    }
}
