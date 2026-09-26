package com.mrcrayfish.guns.util;

import java.util.function.Supplier;

/**
 * Fabric port shim replacing Forge's {@code net.minecraftforge.registries.RegistryObject}.
 * Values are already registered by the time this holder is created (see the register
 * helpers in {@code com.mrcrayfish.guns.init}), so {@link #get()} is a plain accessor.
 */
public class RegistryObject<T> implements Supplier<T>
{
    private final T value;

    private RegistryObject(T value)
    {
        this.value = value;
    }

    public static <T> RegistryObject<T> of(T value)
    {
        return new RegistryObject<>(value);
    }

    @Override
    public T get()
    {
        return this.value;
    }
}
