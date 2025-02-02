package com.ldtteam.structurize.network.messages;

import com.ldtteam.domumornamentum.fabric.NetworkContext;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.fabricmc.api.EnvType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Handles sendScanMessages.
 */
public class SaveScanMessage implements IMessage
{
    private static final String TAG_MILLIS    = "millies";
    public static final  String TAG_SCHEMATIC = "schematic";

    private CompoundTag compoundNBT;
    private String      fileName;

    /**
     * Public standard constructor.
     */
    public SaveScanMessage(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
        {
            final CompoundTag wrapperCompound = NbtIo.readCompressed(stream);
            this.compoundNBT = wrapperCompound.getCompound(TAG_SCHEMATIC);
            this.fileName = wrapperCompound.getString(TAG_MILLIS);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Structure too big to be processed", e);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    /**
     * Send a scan compound to the client.
     *
     * @param CompoundNBT the stream.
     * @param fileName  String with the name of the file.
     */
    public SaveScanMessage(final CompoundTag CompoundNBT, final String fileName)
    {
        this.fileName = fileName;
        this.compoundNBT = CompoundNBT;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        final CompoundTag wrapperCompound = new CompoundTag();
        wrapperCompound.putString(TAG_MILLIS, fileName);
        wrapperCompound.put(TAG_SCHEMATIC, compoundNBT);

        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buffer))
        {
            NbtIo.writeCompressed(wrapperCompound, stream);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
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
        if (compoundNBT != null)
        {
            ClientStructurePackLoader.handleSaveScanMessage(compoundNBT, fileName);
        }
    }
}
