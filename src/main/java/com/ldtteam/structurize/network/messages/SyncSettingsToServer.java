package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

/**
 * Sync player settings to server.
 */
public class SyncSettingsToServer implements IMessage
{
    private final boolean displayShared;

    /**
     * Buffer reading message constructor.
     */
    public SyncSettingsToServer(final FriendlyByteBuf buf)
    {
        this.displayShared = buf.readBoolean();
    }

    /**
     * Send setting data from the client.
     */
    public SyncSettingsToServer()
    {
        this.displayShared = Structurize.getConfig().getClient().displayShared.get();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBoolean(displayShared);
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
        ServerPreviewDistributor.register(ctxIn.getSender(), displayShared);
    }
}
