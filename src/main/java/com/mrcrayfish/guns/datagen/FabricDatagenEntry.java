package com.mrcrayfish.guns.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Fabric datagen entrypoint. DamageTypeGen stays unregistered, exactly like the
 * baseline where its addProvider line was commented out.
 */
public class FabricDatagenEntry implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator)
    {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(RecipeGen::new);
        pack.addProvider(LootTableGen::new);
        pack.addProvider(BlockTagGen::new);
        pack.addProvider(ItemTagGen::new);
        pack.addProvider(GunGen::new);
        //pack.addProvider(DamageTypeGen::new);
    }
}
