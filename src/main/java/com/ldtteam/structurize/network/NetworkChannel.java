package com.ldtteam.structurize.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.network.messages.*;
import com.ldtteam.structurize.network.messages.splitting.SplitPacketMessage;
import io.github.fabricators_of_create.porting_lib.util.ServerLifecycleHooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Our wrapper for Forge network layer
 */
public class NetworkChannel
{
    /**
     * Forge network channel
     */
    private final SimpleChannel rawChannel;

    /**
     * The messages that this channel can process, as viewed from a message id.
     */
    private final Map<Integer, NetworkingMessageEntry<?>> messagesTypes = Maps.newHashMap();

    /**
     * The message that this channel can process, as viewed from a message type.
     */
    private final Map<Class<? extends IMessage>, Integer> messageTypeToIdMap = Maps.newHashMap();

    /**
     * Cache of partially received messages, this holds the data untill it is processed.
     */
    private final Cache<Integer, Map<Integer, byte[]>> messageCache = CacheBuilder.newBuilder()
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .concurrencyLevel(8)
      .build();

    /**
     * An atomic counter which keeps track of the split messages that have been send to somewhere from this network node.
     */
    private final AtomicInteger messageCounter = new AtomicInteger();

    /**
     * Creates a new instance of network channel.
     *
     * @param channelName unique channel name
     * @throws IllegalArgumentException if channelName already exists
     */
    public NetworkChannel(final String channelName)
    {
        final String modVersion = FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
        rawChannel = new SimpleChannel(new ResourceLocation(Constants.MOD_ID, channelName));
    }

    /**
     * Registers all common messages.
     */
    public void registerCommonMessages()
    {
        setupInternalMessages();

        int idx = 1;
        registerMessage(++idx, RemoveBlockMessage.class, RemoveBlockMessage::new);
        registerMessage(++idx, RemoveEntityMessage.class, RemoveEntityMessage::new);
        registerMessage(++idx, SaveScanMessage.class, SaveScanMessage::new);
        registerMessage(++idx, ReplaceBlockMessage.class, ReplaceBlockMessage::new);
        registerMessage(++idx, FillTopPlaceholderMessage.class, FillTopPlaceholderMessage::new);
        registerMessage(++idx, ScanOnServerMessage.class, ScanOnServerMessage::new);
        registerMessage(++idx, ServerUUIDMessage.class, ServerUUIDMessage::new);
        registerMessage(++idx, UndoRedoMessage.class, UndoRedoMessage::new);
        registerMessage(++idx, UpdateScanToolMessage.class, UpdateScanToolMessage::new);
        registerMessage(++idx, UpdateClientRender.class, UpdateClientRender::new);
        registerMessage(++idx, BuildToolPlacementMessage.class, BuildToolPlacementMessage::new);
        registerMessage(++idx, ShowScanMessage.class, ShowScanMessage::new);

        registerMessage(++idx, AddRemoveTagMessage.class, AddRemoveTagMessage::new);
        registerMessage(++idx, SetTagInTool.class, SetTagInTool::new);
        registerMessage(++idx, OperationHistoryMessage.class, OperationHistoryMessage::new);

        registerMessage(++idx, NotifyServerAboutStructurePacksMessage.class, NotifyServerAboutStructurePacksMessage::new);
        registerMessage(++idx, BuildToolPlacementMessage.class, BuildToolPlacementMessage::new);
        registerMessage(++idx, BlueprintSyncMessage.class, BlueprintSyncMessage::new);
        registerMessage(++idx, SyncSettingsToServer.class, SyncSettingsToServer::new);
        registerMessage(++idx, SyncPreviewCacheToServer.class, SyncPreviewCacheToServer::new);

        registerMessage(++idx, NotifyClientAboutStructurePacksMessage.class, NotifyClientAboutStructurePacksMessage::new);
        registerMessage(++idx, TransferStructurePackToClient.class, TransferStructurePackToClient::new);
        registerMessage(++idx, ClientBlueprintRequestMessage.class, ClientBlueprintRequestMessage::new);
        registerMessage(++idx, SyncPreviewCacheToClient.class, SyncPreviewCacheToClient::new);

        registerMessage(++idx, ItemMiddleMouseMessage.class, ItemMiddleMouseMessage::new);
        registerMessage(++idx, ScanToolTeleportMessage.class, ScanToolTeleportMessage::new);
        registerMessage(++idx, AbsorbBlockMessage.class, AbsorbBlockMessage::new);
    }

    private void setupInternalMessages()
    {
        rawChannel.registerC2SPacket(SplitPacketMessage.class, 0, SplitPacketMessage::new);
        rawChannel.registerS2CPacket(SplitPacketMessage.class, 1, SplitPacketMessage::new);
        /*rawChannel.registerMessage(0, SplitPacketMessage.class, IMessage::toBytes, SplitPacketMessage::new, (msg, ctxIn) -> {
            final net.minecraftforge.network.NetworkEvent.Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();
            ctx.setPacketHandled(true);
            msg.onExecute(ctx, packetOrigin.equals(LogicalSide.CLIENT));
        });*/
    }

    /**
     * Register a message into rawChannel.
     *
     * @param <MSG>      message class type
     * @param id         network id
     * @param msgClazz   message class
     * @param msgCreator supplier with new instance of msgClazz
     */
    private <MSG extends IMessage> void registerMessage(final int id, final Class<MSG> msgClazz, final Function<FriendlyByteBuf, MSG> msgCreator)
    {
        this.messagesTypes.put(id, new NetworkingMessageEntry<>(msgCreator, msgClazz));
        this.messageTypeToIdMap.put(msgClazz, id);
    }

    /**
     * Sends to server.
     *
     * @param msg message to send
     */
    public void sendToServer(final IMessage msg)
    {
        handleSplitting(msg, rawChannel::sendToServer);
    }

    /**
     * Sends to player.
     *
     * @param msg    message to send
     * @param player target player
     */
    public void sendToPlayer(final IMessage msg, final ServerPlayer player)
    {
        handleSplitting(msg, s -> rawChannel.sendToClient(s, player));
    }

    /**
     * Sends to origin client.
     *
     * @param msg message to send
     * @param ctx network context
     */
    public void sendToOrigin(final IMessage msg, final NetworkContext ctx)
    {
        final ServerPlayer player = ctx.getSender();
        if (player != null) // side check
        {
            sendToPlayer(msg, player);
        }
        else
        {
            sendToServer(msg);
        }
    }

    /**
     * Sends to everyone in dimension.
     *
     * @param msg message to send
     * @param dim target dimension
     */
    public void sendToDimension(final IMessage msg, final ResourceKey<Level> dim)
    {
        rawChannel.sendToClientsInWorld(msg, ServerLifecycleHooks.getCurrentServer().getLevel(dim));
    }

    /**
     * Sends to everyone in circle made using given target point.
     *
     * @param msg message to send
     * @param pos target position and radius
     */
    public void sendToPosition(final IMessage msg, ServerLevel level, Vec3 pos, double radius)
    {
        handleSplitting(msg, s -> rawChannel.sendToClientsAround(s, level, pos, radius));
    }

    /**
     * Sends to everyone.
     *
     * @param msg message to send
     */
    public void sendToEveryone(final IMessage msg)
    {
        handleSplitting(msg, s -> rawChannel.sendToClientsInCurrentServer(s));
    }

    /**
     * Sends to everyone who is in range from entity's pos using formula below.
     *
     * <pre>
     * Math.min(Entity.getType().getTrackingRange(), ChunkManager.this.viewDistance - 1) * 16;
     * </pre>
     *
     * as of 24-06-2019
     *
     * @param msg    message to send
     * @param entity target entity to look at
     */
    public void sendToTrackingEntity(final IMessage msg, final Entity entity)
    {
        handleSplitting(msg, s -> rawChannel.sendToClientsTracking(s, entity));
    }

    /**
     * Sends to everyone (including given entity) who is in range from entity's pos using formula below.
     *
     * <pre>
     * Math.min(Entity.getType().getTrackingRange(), ChunkManager.this.viewDistance - 1) * 16;
     * </pre>
     *
     * as of 24-06-2019
     *
     * @param msg    message to send
     * @param entity target entity to look at
     */
    public void sendToTrackingEntityAndSelf(final IMessage msg, final Entity entity)
    {
        handleSplitting(msg, s -> rawChannel.sendToClientsTrackingAndSelf(s, entity));
    }

    /**
     * Sends to everyone in given chunk.
     *
     * @param msg   message to send
     * @param chunk target chunk to look at
     */
    public void sendToTrackingChunk(final IMessage msg, final LevelChunk chunk)
    {
        handleSplitting(msg, s -> rawChannel.sendToClientsTracking(s, (ServerLevel) chunk.getLevel(), chunk.getPos()));
    }

    /**
     * Method that handles the splitting of the message into chunks if need be.
     *
     * @param msg                  The message to split in question.
     * @param splitMessageConsumer The consumer that sends away the split parts of the message.
     */
    private void handleSplitting(final IMessage msg, final Consumer<IMessage> splitMessageConsumer)
    {
        //Get the inner message id and check if it is known.
        final int messageId = this.messageTypeToIdMap.getOrDefault(msg.getClass(), -1);
        if (messageId == -1)
        {
            throw new IllegalArgumentException("The message is unknown to this channel!");
        }

        //Write the message into a buffer and copy that buffer into a byte array for processing.
        final ByteBuf buffer = Unpooled.buffer();
        final FriendlyByteBuf innerFriendlyByteBuf = new FriendlyByteBuf(buffer);
        msg.toBytes(innerFriendlyByteBuf);
        final byte[] data = buffer.array();
        buffer.release();

        //Some tracking variables.
        //Max packet size: 90% of maximum.
        final int max_packet_size = msg.getExecutionSide() == EnvType.SERVER ? 30000 : 943718; //This is 90% of max packet size.
        //The current index in the data array.
        int currentIndex = 0;
        //The current index for the split packets.
        int packetIndex = 0;
        //The communication id.
        final int comId = messageCounter.getAndIncrement();

        //Loop while data is available.
        while (currentIndex < data.length)
        {
            //Tell the network message entry that we are splitting a packet.
            this.getMessagesTypes().get(messageId).onSplitting(packetIndex);

            final int extra = Math.min(max_packet_size, data.length - currentIndex);
            //Extract the sub data array.
            final byte[] subPacketData = Arrays.copyOfRange(data, currentIndex, currentIndex + extra);

            //Construct the wrapping packet.
            final SplitPacketMessage splitPacketMessage = new SplitPacketMessage(comId, packetIndex++, (currentIndex + extra) >= data.length, messageId, subPacketData);

            //Send the wrapping packet.
            splitMessageConsumer.accept(splitPacketMessage);

            //Move our working index.
            currentIndex += extra;
        }
    }

    /**
     * Gives access to the cache of messages that are being received.
     *
     * @return The message cache.
     */
    public Cache<Integer, Map<Integer, byte[]>> getMessageCache()
    {
        return messageCache;
    }

    /**
     * Gives access to the internal index codec.
     *
     * @return The internal index codec map.
     */
    public Map<Integer, NetworkingMessageEntry<?>> getMessagesTypes()
    {
        return messagesTypes;
    }

    /**
     * A class that handles the data wrapping for our inner index codec.
     *
     * @param <MSG> The message type.
     */
    public static final class NetworkingMessageEntry<MSG extends IMessage>
    {
        /**
         * Atomic boolean that tracks if a splitting warning has been written to the log for a given packet type.
         */
        private final AtomicBoolean hasWarned = new AtomicBoolean(true);

        /**
         * A callback to create a new message instance.
         */
        private final Function<FriendlyByteBuf, MSG> creator;

        /**
         * The message class.
         */
        private final Class<? extends IMessage> clazz;

        /**
         * Create a new message entry.
         * @param creator the creator of the message.
         * @param clazz its class.
         */
        private NetworkingMessageEntry(final Function<FriendlyByteBuf, MSG> creator, final Class<? extends IMessage> clazz)
        {
            this.creator = creator;
            this.clazz = clazz;
        }

        /**
         * Gives access to the callback that creates a new message instance.
         *
         * @return The callback.
         */
        public Function<FriendlyByteBuf, MSG> getCreator()
        {
            return creator;
        }

        /**
         * Invoked to indicate that a packet is being split.
         *
         * @param packetIndex The index of the split packet that is being send.
         */
        public void onSplitting(int packetIndex)
        {
            //We only log when the SECOND packet, so with index 1, is processed.
            if (packetIndex != 1)
            {
                return;
            }

            //Ensure we only log once for a given packet.
            if (hasWarned.getAndSet(false))
            {
                Log.getLogger().warn("Splitting message: " + clazz + " it is too big to send normally. This message is only printed once");
            }
        }
    }
}
