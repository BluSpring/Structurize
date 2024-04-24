package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.management.Manager;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Class handling the Server UUID Message.
 */
public class ServerUUIDMessage implements IMessage
{
    private final UUID serverUUID;

    /**
     * Empty constructor used when registering the message.
     */
    public ServerUUIDMessage()
    {
        this.serverUUID = Manager.getServerUUID();
    }

    public ServerUUIDMessage(final FriendlyByteBuf buf)
    {
        this.serverUUID = buf.readUUID();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUUID(Manager.getServerUUID());
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
        Manager.setServerUUID(serverUUID);
    }
}
