package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.items.ItemScanTool;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScanToolTeleportMessage implements IMessage
{
    public ScanToolTeleportMessage()
    {
    }

    public ScanToolTeleportMessage(@NotNull final FriendlyByteBuf buf)
    {
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
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
        final ItemStack stack = ctxIn.getSender().getMainHandItem();
        if (stack.getItem() instanceof ItemScanTool tool)
        {
            tool.onTeleport(ctxIn.getSender(), stack);
        }
    }
}
