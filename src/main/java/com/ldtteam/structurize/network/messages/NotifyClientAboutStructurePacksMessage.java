package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import com.ldtteam.structurize.storage.StructurePackMeta;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Notify the client about the structure packs on the server side.
 */
public class NotifyClientAboutStructurePacksMessage implements IMessage
{
    /**
     * List of server structure packs.
     * Contains String Name, and Integer version.
     */
    private final Map<String, Double> serverStructurePacks = new HashMap<>();

    /**
     * Public standard constructor.
     */
    public NotifyClientAboutStructurePacksMessage(final FriendlyByteBuf buf)
    {
        final int length = buf.readInt();
        for (int i = 0; i < length; i++)
        {
            this.serverStructurePacks.put(buf.readUtf(32767), buf.readDouble());
        }
    }

    /**
     * Notify the client about the server structurepacks.
     * @param clientStructurePacks the list of packs.
     */
    public NotifyClientAboutStructurePacksMessage(final Map<String, StructurePackMeta> clientStructurePacks)
    {
        for (final StructurePackMeta pack : clientStructurePacks.values())
        {
            if (!pack.isImmutable())
            {
                this.serverStructurePacks.put(pack.getName(), pack.getVersion());
            }
        }
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.serverStructurePacks.size());
        for (final Map.Entry<String, Double> packInfo : this.serverStructurePacks.entrySet())
        {
            buf.writeUtf(packInfo.getKey());
            buf.writeDouble(packInfo.getValue());
        }
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
            ClientStructurePackLoader.onServerSyncAttempt(this.serverStructurePacks);
        }
    }
}
