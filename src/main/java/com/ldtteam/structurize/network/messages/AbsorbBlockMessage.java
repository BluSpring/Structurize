package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.items.ItemTagSubstitution;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sent client to server to request that the currently held item should "absorb" a new replacement block.
 */
public class AbsorbBlockMessage implements IMessage
{
    private final BlockPos pos;
    private final ItemStack stack;

    /**
     * Construct
     * @param pos the location of the block to absorb (in the sender's level)
     * @param stack the "picked item" stack produced by that block
     */
    public AbsorbBlockMessage(@NotNull final BlockPos pos, @NotNull final ItemStack stack)
    {
        this.pos = pos;
        this.stack = stack;
    }

    /**
     * Deserialize
     * @param buf the network buffer
     */
    public AbsorbBlockMessage(@NotNull final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.stack = buf.readItem();
    }

    /**
     * Serialize
     * @param buf network data byte buffer
     */
    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeItem(this.stack);
    }

    @Nullable
    @Override
    public EnvType getExecutionSide()
    {
        return EnvType.SERVER;
    }

    @Override
    public void onExecute(@NotNull final NetworkContext ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        final ItemStack current = player.getInventory().getSelected();

        if (current.getItem() instanceof ItemTagSubstitution anchor)
        {
            anchor.onAbsorbBlock(player, current, this.pos, this.stack);
        }
    }
}
