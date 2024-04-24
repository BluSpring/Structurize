package com.ldtteam.structurize.config;

import com.google.common.collect.Lists;
import io.github.fabricators_of_create.porting_lib.config.ModConfigSpec;
import net.minecraft.core.Direction;

import java.util.List;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    /**
     * Should the default schematics be ignored (from the jar)?
     */
    public final ModConfigSpec.BooleanValue ignoreSchematicsFromJar;

    /**
     * Should player made schematics be allowed
     */
    public final ModConfigSpec.BooleanValue allowPlayerSchematics;

    /**
     * Max world operations per tick (Max blocks to place, remove or replace)
     */
    public final ModConfigSpec.IntValue maxOperationsPerTick;

    /**
     * Max amount of changes cached to be able to undo
     */
    public final ModConfigSpec.IntValue maxCachedChanges;

    /**
     * Max amount of schematics to be cached on the server
     */
    public final ModConfigSpec.IntValue maxCachedSchematics;

    /**
     * Max amount of blocks checked by a possible worker.
     */
    public final ModConfigSpec.IntValue maxBlocksChecked;

    /**
     * Max amount of blocks checked by a possible worker.
     */
    public final ModConfigSpec.IntValue schematicBlockLimit;

    public final ModConfigSpec.ConfigValue<String> iteratorType;

    public final ModConfigSpec.ConfigValue<List<Integer>> updateStartPos;

    public final ModConfigSpec.ConfigValue<List<Integer>> updateEndPos;

    public final ModConfigSpec.BooleanValue teleportAllowed;
    public final ModConfigSpec.EnumValue<Direction> teleportBuildDirection;
    public final ModConfigSpec.IntValue teleportBuildDistance;
    public final ModConfigSpec.BooleanValue teleportSafety;

    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    protected ServerConfiguration(final ModConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");

        ignoreSchematicsFromJar = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        allowPlayerSchematics = defineBoolean(builder, "allowPlayerSchematics", true);
        maxOperationsPerTick = defineInteger(builder, "maxOperationsPerTick", 1000, 0, 100000);
        maxCachedChanges = defineInteger(builder, "maxCachedChanges", 50, 0, 250);
        maxCachedSchematics = defineInteger(builder, "maxCachedSchematics", 100, 0, 100000);
        maxBlocksChecked = defineInteger(builder, "maxBlocksChecked", 1000, 0, 100000);
        schematicBlockLimit = defineInteger(builder, "schematicBlockLimit", 100000, 1000, 1000000);
        iteratorType = defineString(builder, "iteratorType", "default");

        swapToCategory(builder, "teleport");

        teleportAllowed = defineBoolean(builder, "teleportAllowed", true);
        teleportBuildDirection = defineEnum(builder, "teleportBuildDirection", Direction.SOUTH);
        teleportBuildDistance = defineInteger(builder, "teleportBuildDistance", 3, 1, 16);
        teleportSafety = defineBoolean(builder, "teleportSafety", true);

        swapToCategory(builder, "update");

        updateStartPos = builder.define("start", Lists.newArrayList(-10,-10));
        updateEndPos = builder.define("end", Lists.newArrayList(10,10));

        finishCategory(builder);
    }
}