package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.client.gui.WindowUndoRedo;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.ChangeStorage;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OperationHistoryMessage implements IMessage
{
    /**
     * List of operations and their IDs
     */
    private List<Tuple<String, Integer>> operationIDs = new ArrayList<>();

    /**
     * Empty constructor used when registering the
     */
    public OperationHistoryMessage(final FriendlyByteBuf buf)
    {
        final int count = buf.readInt();
        operationIDs = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            operationIDs.add(new Tuple<>(buf.readUtf(), buf.readInt()));
        }
    }

    public OperationHistoryMessage()
    {

    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(operationIDs.size());
        for (final Tuple<String, Integer> operation : operationIDs)
        {
            buf.writeUtf(operation.getA());
            buf.writeInt(operation.getB());
        }
    }

    @Nullable
    @Override
    public EnvType getExecutionSide()
    {
        return null;
    }

    @Override
    public void onExecute(final NetworkContext ctxIn, final boolean isLogicalServer)
    {
        if (isLogicalServer)
        {
            if (ctxIn.getSender() == null)
            {
                return;
            }

            final List<ChangeStorage> operations = Manager.getChangeStoragesForPlayer(ctxIn.getSender().getUUID());
            operationIDs = new ArrayList<>();
            for (final ChangeStorage storage : operations)
            {
                operationIDs.add(new Tuple<>(storage.getOperation().getString(), storage.getID()));
            }

            Network.getNetwork().sendToPlayer(this, ctxIn.getSender());
        }
        else
        {
            WindowUndoRedo.lastOperations = operationIDs;
        }
    }
}
