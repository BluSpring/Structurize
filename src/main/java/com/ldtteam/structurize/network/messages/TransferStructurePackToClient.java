package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

/**
 * Transfer a zipped structure pack to the client.
 */
public class TransferStructurePackToClient implements IMessage
{
    /**
     * Payload of the message (to transfer to client).
     */
    private final ByteBuf payload;

    /**
     * The name of the structure pack.
     */
    private final String packname;

    /**
     * If the last message.
     */
    private final boolean eol;

    /**
     * Public standard constructor.
     */
    public TransferStructurePackToClient(final FriendlyByteBuf buf)
    {
        this.packname = buf.readUtf(32767);
        this.eol = buf.readBoolean();
        this.payload = Unpooled.wrappedBuffer(buf.readByteArray());
    }

    /**
     * Transfer a zipped structure pack to the client.
     * @param packName the name of the structure pack.
     * @param payload the payload.
     * @param eol if last message to client.
     */
    public TransferStructurePackToClient(final String packName, final ByteBuf payload, final boolean eol)
    {
        this.packname = packName;
        this.payload = payload;
        this.eol = eol;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(this.packname);
        buf.writeBoolean(this.eol);
        buf.writeByteArray(this.payload.array());
        this.payload.release();
    }

    @Nullable
    @Override
    public EnvType getExecutionSide()
    {
        return EnvType.CLIENT;
    }

    @Override
    public void onExecute(final NetworkContext ctxIn, final boolean isLogicalServer)
    {
        if (!isLogicalServer)
        {
            ClientStructurePackLoader.onStructurePackTransfer(this.packname, this.payload, this.eol);
        }
    }
}
