package com.ldtteam.structurize.blockentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities
{
    private ModBlockEntities() { /* prevent construction */ }

    private static final LazyRegistrar<BlockEntityType<?>> BLOCK_ENTITIES = LazyRegistrar.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static LazyRegistrar<BlockEntityType<?>> getRegistry()
    {
        return BLOCK_ENTITIES;
    }

    public static RegistryObject<BlockEntityType<BlockEntityTagSubstitution>> TAG_SUBSTITUTION = getRegistry().register("tagsubstitution",
      () -> BlockEntityType.Builder.of(BlockEntityTagSubstitution::new, ModBlocks.blockTagSubstitution.get()).build(null));
}
