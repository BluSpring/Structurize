package com.ldtteam.structurize.client;

import com.ldtteam.structurize.event.ClientEventSubscriber;
import com.ldtteam.structurize.event.ClientLifecycleSubscriber;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public class StructurizeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientStructurePackLoader.onClientLoading();
        ClientStructurePackLoader.init();
        ClientFutureProcessor.init();
        ClientLifecycleSubscriber.init();
        ClientEventSubscriber.init();

        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.blockTagSubstitution.get(), ((stack, mode, matrices, vertexConsumers, light, overlay) -> {
            TagSubstitutionRenderer.getInstance().renderByItem(stack, mode, matrices, vertexConsumers, light, overlay);
        }));
    }
}
