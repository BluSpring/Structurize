package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.util.ScanToolData;
import net.fabricmc.api.EnvType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Send the scan tool update message to the client.
 */
public class UpdateScanToolMessage implements IMessage
{
    /**
     * Data.
     */
    private final CompoundTag tag;

    /**
     * Empty public constructor.
     */
    public UpdateScanToolMessage(final FriendlyByteBuf buf)
    {
        this.tag = buf.readNbt();
    }

    /**
     * Update the scan tool.
     * @param data the new data
     */
    public UpdateScanToolMessage(@NotNull ScanToolData data)
    {
        this.tag = data.getInternalTag().copy();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeNbt(this.tag);
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
            stack.setTag(this.tag);
            tool.loadSlot(new ScanToolData(stack.getOrCreateTag()), stack);
        }
    }
}
