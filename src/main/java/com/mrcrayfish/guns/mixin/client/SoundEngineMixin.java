package com.mrcrayfish.guns.mixin.client;

import com.mrcrayfish.guns.client.handler.SoundHandler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric replacement for Forge's PlaySoundEvent: lets SoundHandler wrap sounds
 * (deafness mute) before they are played. The wrapper is applied once; a wrapped
 * instance passes through unchanged, so re-dispatching here terminates.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin
{
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void cgm$onPlaySound(SoundInstance sound, CallbackInfo ci)
    {
        SoundInstance replacement = SoundHandler.get().onPlaySound(sound);
        if (replacement != sound)
        {
            ci.cancel();
            ((SoundEngine) (Object) this).play(replacement);
        }
    }
}
