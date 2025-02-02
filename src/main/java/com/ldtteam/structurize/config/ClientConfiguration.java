package com.ldtteam.structurize.config;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.network.messages.SyncSettingsToServer;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import io.github.fabricators_of_create.porting_lib.config.ModConfigSpec;
import io.github.fabricators_of_create.porting_lib.config.ModConfigSpec.BooleanValue;
import io.github.fabricators_of_create.porting_lib.config.ModConfigSpec.ConfigValue;
import io.github.fabricators_of_create.porting_lib.config.ModConfigSpec.DoubleValue;
import io.github.fabricators_of_create.porting_lib.config.ModConfigSpec.IntValue;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

/**
 * Mod client configuration.
 * Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    // blueprint renderer

    public final BooleanValue renderPlaceholdersNice;
    public final BooleanValue sharePreviews;
    public final BooleanValue displayShared;
    public final IntValue rendererLightLevel;
    public final DoubleValue rendererTransparency;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ModConfigSpec.Builder builder)
    {
        createCategory(builder, "blueprint.renderer");
        // if you add anything to this category, also add it #collectPreviewRendererSettings()
        
        renderPlaceholdersNice = defineBoolean(builder, "render_placeholders_nice", false);
        sharePreviews = defineBoolean(builder, "share_previews", false);
        displayShared = defineBoolean(builder, "see_shared_previews", false);
        rendererLightLevel = defineInteger(builder, "light_level", 15, -1, 15);
        rendererTransparency = defineDouble(builder, "transparency", -1, -1, 1);

        addWatcher(BlueprintHandler.getInstance()::clearCache, renderPlaceholdersNice, rendererLightLevel);
        addWatcher(displayShared, (oldValue, isSharingEnabled) -> {
            // notify server
            Network.getNetwork().sendToServer(new SyncSettingsToServer());
            if (!isSharingEnabled)
            {
                RenderingCache.removeSharedPreviews();
            }
        });
        addWatcher(sharePreviews, (oldVal, shouldSharePreviews) -> {
            if (shouldSharePreviews)
            {
                RenderingCache.getBlueprintsToRender().forEach(BlueprintPreviewData::syncChangesToServer);
            }
        });

        finishCategory(builder);
    }

    /**
     * Things which should be in buildtool settings, order is mostly carried over to gui order
     */
    public void collectPreviewRendererSettings(final Consumer<ConfigValue<?>> sink)
    {
        sink.accept(sharePreviews);
        sink.accept(displayShared);
        sink.accept(renderPlaceholdersNice);
        sink.accept(rendererLightLevel);
        sink.accept(rendererTransparency);
    }
}
