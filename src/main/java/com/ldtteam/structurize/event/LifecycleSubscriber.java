package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.util.LanguageHandler;

public class LifecycleSubscriber
{
    public static void init() {
        onModInit();

        onLoadComplete();
    }

    /**
     * Called when mod is being initialized.
     *
     */
    public static void onModInit()
    {
        Network.getNetwork().registerCommonMessages();
    }

    /**
     * Called when MC loading is about to finish.
     *
     */
    public static void onLoadComplete()
    {
        LanguageHandler.setMClanguageLoaded();
    }

    /*@SubscribeEvent
    public static void onDatagen(@NotNull final GatherDataEvent event)
    {
        final DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeServer(), new BlockEntityTagProvider(event.getGenerator().getPackOutput(), Registries.BLOCK_ENTITY_TYPE, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new BlockTagProvider(event.getGenerator().getPackOutput(), Registries.BLOCK, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new EntityTagProvider(event.getGenerator().getPackOutput(), Registries.ENTITY_TYPE, event.getLookupProvider(), event.getExistingFileHelper()));
    }*/
}
