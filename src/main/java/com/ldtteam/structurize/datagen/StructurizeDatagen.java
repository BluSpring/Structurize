package com.ldtteam.structurize.datagen;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.registries.Registries;

public class StructurizeDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var generator = fabricDataGenerator.createPack();
        ExistingFileHelper fileHelper = null;

        generator.addProvider((output, lookup) -> new BlockEntityTagProvider(output, Registries.BLOCK_ENTITY_TYPE, lookup, fileHelper));
        generator.addProvider((output, lookup) -> new BlockTagProvider(output, Registries.BLOCK, lookup, fileHelper));
        generator.addProvider((output, lookup) -> new EntityTagProvider(output, Registries.ENTITY_TYPE, lookup, fileHelper));
    }
}
