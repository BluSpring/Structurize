package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Messages for adding or removing a tag
 */
public class SetTagInTool implements IMessage
{
    /**
     * The tag to use
     */
    private String tag = "";

    /**
     * The tags blockpos
     */
    private final int slot;

    /**
     * Empty constructor used when registering the
     */
    public SetTagInTool(final FriendlyByteBuf buf)
    {
        this.tag = buf.readUtf(32767);
        this.slot = buf.readInt();
    }

    public SetTagInTool(final String tag, final int slot)
    {
        this.slot = slot;
        this.tag = tag;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(tag);
        buf.writeInt(slot);
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
        if (ctxIn.getSender() == null)
        {
            return;
        }

        if (!ctxIn.getSender().isCreative())
        {
            ctxIn.getSender().displayClientMessage(Component.translatable("structurize.gui.tagtool.creative_only"), false);
            return;
        }

        final ItemStack stack = ctxIn.getSender().getInventory().getItem(slot);
        if (stack.getItem() == ModItems.tagTool.get())
        {
            stack.getOrCreateTag().putString(ItemTagTool.TAG_CURRENT_TAG, tag);
        }
    }
}
