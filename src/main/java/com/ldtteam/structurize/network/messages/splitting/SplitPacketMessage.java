package com.ldtteam.structurize.network.messages.splitting;

import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.fabric.NetworkHelper;
import com.ldtteam.structurize.network.NetworkChannel;
import com.ldtteam.structurize.network.messages.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Represents a class that wrappers other messages in byte form and is used to split the wrapped messages data into several chunks.
 */
public class SplitPacketMessage implements IMessage
{
    /**
     * Internal communication id. Used to indicate to what wrapped message this belongs to.
     */
    private int communicationId = -1;

    /**
     * The index of the split message in the wrapped message.
     */
    private int packetIndex = -1;

    /**
     * Indicates if this is the last message in the chain.
     */
    private boolean terminator = false;

    /**
     * The id of the message inside the splitting logic. Identical to the index codec system in SimpleChannel-
     */
    private int innerMessageId = -1;

    /**
     * The payload.
     */
    private byte[] payload;

    /**
     * The network receiving constructor.
     */
    public SplitPacketMessage(final FriendlyByteBuf buf)
    {
        this.communicationId = buf.readVarInt();
        this.packetIndex = buf.readVarInt();
        this.terminator = buf.readBoolean();
        this.innerMessageId = buf.readVarInt();
        this.payload = buf.readByteArray();
    }

    public SplitPacketMessage(final int communicationId, final int packetIndex, final boolean terminator, final int innerMessageId, final byte[] payload)
    {
        this.communicationId = communicationId;
        this.packetIndex = packetIndex;
        this.terminator = terminator;
        this.innerMessageId = innerMessageId;
        this.payload = payload;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.communicationId);
        buf.writeVarInt(this.packetIndex);
        buf.writeBoolean(this.terminator);
        buf.writeVarInt(this.innerMessageId);
        buf.writeByteArray(this.payload);
    }

    @Nullable
    @Override
    public EnvType getExecutionSide()
    {
        return null;
    }

    @Override
    public void onExecute(final NetworkContext ctxIn, final boolean isLogicalServer)
    {
        try
        {
            //Sync on the message cache since this is still on the Netty thread.
            synchronized (Network.getNetwork().getMessageCache())
            {
                Network.getNetwork().getMessageCache().get(this.communicationId, Maps::newConcurrentMap).put(this.packetIndex, this.payload);
            }

            if (!this.terminator)
            {
                //We are not the last message stop executing.
                return;
            }

            //No need to sync again, since we are now the last packet to arrive.
            //All data gets sorted and appended.
            final byte[] packetData = Network.getNetwork().getMessageCache().get(this.communicationId, Maps::newConcurrentMap).entrySet()
                                        .stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .map(Map.Entry::getValue)
                                        .reduce(new byte[0], Bytes::concat);

            //Grab the entry from the inner message id.
            final NetworkChannel.NetworkingMessageEntry<?> messageEntry = Network.getNetwork().getMessagesTypes().get(this.innerMessageId);

            //Create a new buffer that reads from the packet data and then deserialize the inner message.
            final ByteBuf buffer = Unpooled.wrappedBuffer(packetData);
            //Create a message.
            final IMessage message = messageEntry.getCreator().apply(new FriendlyByteBuf(buffer));
            buffer.release();

            //Execute the message.
            final EnvType packetOrigin = ctxIn.direction().getOriginationSide();
            if (message.getExecutionSide() != null && packetOrigin.equals(message.getExecutionSide()))
            {
                Log.getLogger().warn("Receving {} at wrong side!", message.getClass().getName());
                return;
            }
            // boolean param MUST equals true if packet arrived at logical server
            NetworkHelper.enqueueWork(isLogicalServer, () -> message.onExecute(ctxIn, packetOrigin.equals(EnvType.CLIENT)));
        }
        catch (ExecutionException e)
        {
            Log.getLogger().error("Failed to handle split packet.", e);
        }
    }
}
