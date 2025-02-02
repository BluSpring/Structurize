package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Waits for blueprint futures to finish loading and then processes them.
 */
public class ServerFutureProcessor
{
    /**
     * Queue for processing blueprint futures.
     */
    private static final Queue<BlueprintProcessingData> blueprintConsumerQueue = new LinkedList<>();

    /**
     * Queue for processing blueprint data futures.
     */
    private static final Queue<BlueprintDataProcessingData> blueprintDataConsumerQueue = new LinkedList<>();

    /**
     * Queue for processing blueprint futures.
     */
    private static final Queue<BlueprintListProcessingData> blueprintListConsumerQueue = new LinkedList<>();

    /**
     * Queue processing data to be handled on tick.
     * @param processingData the data to be processed.
     */
    public static void queueBlueprint(@NotNull final BlueprintProcessingData processingData)
    {
        blueprintConsumerQueue.add(processingData);
    }

    /**
     * Queue processing data to be handled on tick.
     * @param processingData the data to be processed.
     */
    public static void queueBlueprintList(@NotNull final BlueprintListProcessingData processingData)
    {
        blueprintListConsumerQueue.add(processingData);
    }

    /**
     * Queue processing data to be handled on tick.
     * @param processingData the data to be processed.
     */
    public static void queueBlueprintData(@NotNull final BlueprintDataProcessingData processingData)
    {
        blueprintDataConsumerQueue.add(processingData);
    }

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            onWorldTick(world);
        });
    }

    public static void onWorldTick(ServerLevel level)
    {
        //if (event.phase == TickEvent.Phase.END)
        {
            if (!blueprintConsumerQueue.isEmpty() && blueprintConsumerQueue.peek().level == level && blueprintConsumerQueue.peek().blueprintFuture.isDone())
            {
                final BlueprintProcessingData data = blueprintConsumerQueue.poll();
                try
                {
                    data.consumer.accept(data.blueprintFuture.get());
                }
                catch (InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }
            }

            if (!blueprintDataConsumerQueue.isEmpty() && blueprintDataConsumerQueue.peek().level == level && blueprintDataConsumerQueue.peek().blueprintDataFuture.isDone())
            {
                final BlueprintDataProcessingData data = blueprintDataConsumerQueue.poll();
                try
                {
                    data.consumer.accept(data.blueprintDataFuture.get());
                }
                catch (InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }
            }

            if (!blueprintListConsumerQueue.isEmpty() && blueprintListConsumerQueue.peek().level == level && blueprintListConsumerQueue.peek().blueprintFuture.isDone())
            {
                final BlueprintListProcessingData data = blueprintListConsumerQueue.poll();
                try
                {
                    data.consumer.accept(data.blueprintFuture.get());
                }
                catch (InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Data to be processed.
     */
    public record BlueprintProcessingData(Future<Blueprint> blueprintFuture, Level level, Consumer<Blueprint> consumer) { }

    /**
     * Data to be processed.
     */
    public record BlueprintDataProcessingData(Future<byte[]> blueprintDataFuture, Level level, Consumer<byte[]> consumer) { }

    /**
     * Data to be processed.
     */
    public record BlueprintListProcessingData(Future<List<Blueprint>> blueprintFuture, Level level, Consumer<List<Blueprint>> consumer) { }
}
