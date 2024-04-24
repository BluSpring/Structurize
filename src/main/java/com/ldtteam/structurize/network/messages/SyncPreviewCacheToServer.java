package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

/**
 * Sync blueprint preview data to the server.
 */
public class SyncPreviewCacheToServer implements IMessage
{
    /**
     * The preview data.
     */
    private final BlueprintPreviewData previewData;

    /**
     * Buffer reading message constructor.
     */
    public SyncPreviewCacheToServer(final FriendlyByteBuf buf)
    {
        this.previewData = new BlueprintPreviewData(buf);
    }

    /**
     * Send preview data from the client.
     */
    public SyncPreviewCacheToServer(final BlueprintPreviewData previewData)
    {
        this.previewData = previewData;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        this.previewData.writeToBuf(buf);
    }

    @Nullable
    @Override
    public EnvType getExecutionSide()
    {
        return EnvType.SERVER;
    }

    @Override
    public void onExecute(final NetworkContext ctxIn, final boolean isLogicalServer)
    {
        ServerPreviewDistributor.distribute(this.previewData, ctxIn.getSender());
    }
}
