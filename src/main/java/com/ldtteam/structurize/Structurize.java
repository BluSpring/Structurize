package com.ldtteam.structurize;

import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.DataFixerUtils;
import com.ldtteam.structurize.blueprints.v1.DataVersion;
import com.ldtteam.structurize.config.Configuration;
import com.ldtteam.structurize.event.EventSubscriber;
import com.ldtteam.structurize.event.LifecycleSubscriber;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.proxy.ClientProxy;
import com.ldtteam.structurize.proxy.IProxy;
import com.ldtteam.structurize.proxy.ServerProxy;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.ServerStructurePackLoader;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.datafix.DataFixers;

/**
 * Mod main class.
 * The value in annotation should match an entry in the META-INF/mods.toml file.
 */
public class Structurize implements ModInitializer
{
    /**
     * The proxy.
     */
    public static final IProxy proxy = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? new ClientProxy() : new ServerProxy();

    /**
     * The config instance.
     */
    private static Configuration config;

    /**
     * Mod init, registers events to their respective busses
     */
    public void onInitialize()
    {
        preInit();

        config = new Configuration(FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElseThrow());

        ModBlocks.getRegistry().register();
        ModItems.getRegistry().register();
        ModBlockEntities.getRegistry().register();
        ModItemGroups.TAB_REG.register();

        LifecycleSubscriber.init();
        EventSubscriber.init();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            ServerStructurePackLoader.onServerStarting();
        }

        ServerStructurePackLoader.init();
        ServerPreviewDistributor.init();
        ServerFutureProcessor.init();

        if (DataFixerUtils.isVanillaDF)
        {
            if ((DataFixers.getDataFixer().getSchema(Integer.MAX_VALUE - 1).getVersionKey()) >= DataVersion.UPCOMING.getDataVersion() * 10)
            {
                throw new RuntimeException("You are trying to run old mod on much newer vanilla. Missing some newest data versions. Please update com/ldtteam/structures/blueprints/v1/DataVersion");
            }
            else if (FabricLoader.getInstance().isDevelopmentEnvironment() && DataVersion.CURRENT == DataVersion.UPCOMING)
            {
                throw new RuntimeException("Missing some newest data versions. Please update com/ldtteam/structures/blueprints/v1/DataVersion");
            }
        }
        else
        {
            Log.getLogger().error("----------------------------------------------------------------- \n "
                                    + "Invalid DataFixer detected, schematics might not paste correctly! \n"
                                    +  "The following DataFixer was added: " + DataFixers.getDataFixer().getClass() + "\n"
                                    + "-----------------------------------------------------------------");
        }
    }

    public static void preInit()
    {
        Network.getNetwork().registerCommonMessages();
    }


    /**
     * Get the config handler.
     *
     * @return the config handler.
     */
    public static Configuration getConfig()
    {
        return config;
    }
}
