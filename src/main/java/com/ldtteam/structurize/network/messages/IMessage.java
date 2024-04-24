package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import io.github.fabricators_of_create.porting_lib.util.NetworkDirection;
import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for all network messages
 */
public interface IMessage extends C2SPacket, S2CPacket
{
    /**
     * Writes message data to buffer.
     *
     * @param buf network data byte buffer
     */
    void toBytes(final FriendlyByteBuf buf);

    /**
     * Which sides is message able to be executed at.
     *
     * @return CLIENT or SERVER or null (for both)
     */
    @Nullable
    EnvType getExecutionSide();

    /**
     * Executes message action.
     *
     * @param ctxIn           network context of incoming message
     * @param isLogicalServer whether message arrived at logical server side
     */
    void onExecute(final NetworkContext ctxIn, final boolean isLogicalServer);

    @Override
    default void encode(FriendlyByteBuf buf) {
        this.toBytes(buf);
    }

    @Override
    default void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        onExecute(new NetworkContext(listener.connection, NetworkDirection.PLAY_TO_SERVER), true);
    }

    @Environment(EnvType.CLIENT)
    @Override
    default void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        onExecute(new NetworkContext(listener.getConnection(), NetworkDirection.PLAY_TO_CLIENT), false);
    }
}
