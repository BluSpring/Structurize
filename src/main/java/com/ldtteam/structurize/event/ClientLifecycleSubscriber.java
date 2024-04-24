package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.ClientItemStackTooltip;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.client.TagSubstitutionRenderer;
import com.ldtteam.structurize.client.model.OverlaidModelLoader;
import com.ldtteam.structurize.items.ItemStackTooltip;
import dev.architectury.registry.client.gui.ClientTooltipComponentRegistry;
import io.github.fabricators_of_create.porting_lib.config.ConfigEvents;
import io.github.fabricators_of_create.porting_lib.config.ModConfig;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.RegisterGeometryLoadersCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ClientLifecycleSubscriber
{
    public static void init() {
        onClientInit();
        doClientStuff();

        RegisterGeometryLoadersCallback.EVENT.register(loaders -> {
            registerGeometry(loaders);
        });

        registerRenderers();
        registerTooltips();
        registerKeys();

        ConfigEvents.LOADING.register(config -> {
            if (config.getModId().equals(Constants.MOD_ID)) {
                onConfigLoad(config);
            }
        });

        ConfigEvents.RELOADING.register(config -> {
            if (config.getModId().equals(Constants.MOD_ID)) {
                onConfigEdit(config);
            }
        });
    }

    /**
     * Called when client app is initialized.
     *
     */
    public static void onClientInit()
    {
        final ResourceManager rm = Minecraft.getInstance().getResourceManager();
        if (rm instanceof final ReloadableResourceManager resourceManager)
        {
            resourceManager.registerReloadListener(new SimplePreparableReloadListener<>()
            {

                @Override
                protected Object prepare(final ResourceManager manager, final ProfilerFiller profiler)
                {
                    return new Object();
                }

                @Override
                protected void apply(final Object source, final ResourceManager manager, final ProfilerFiller profiler)
                {
                    Log.getLogger().debug("Clearing blueprint renderer cache.");
                    BlueprintHandler.getInstance().clearCache();
                }
            });
        }
    }

    @Environment(EnvType.CLIENT)
    public static void doClientStuff()
    {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.blockSubstitution.get(), RenderType.translucent());
    }

    public static void registerGeometry(Map<ResourceLocation, IGeometryLoader<?>> loaders)
    {
        loaders.put(new ResourceLocation(Constants.MOD_ID, "overlaid"), new OverlaidModelLoader());
    }

    public static void registerRenderers()
    {
        BlockEntityRenderers.register(ModBlockEntities.TAG_SUBSTITUTION.get(), TagSubstitutionRenderer::new);
    }

    public static void registerTooltips()
    {
        ClientTooltipComponentRegistry.register(ItemStackTooltip.class, ClientItemStackTooltip::new);
    }

    public static void registerKeys()
    {
        ModKeyMappings.register();
    }

    public static void onConfigLoad(ModConfig config)
    {
        Structurize.getConfig().onConfigLoad(config);
    }

    public static void onConfigEdit(ModConfig config)
    {
        Structurize.getConfig().onConfigReload(config);
    }
}
