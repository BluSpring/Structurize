package com.ldtteam.structurize.storage.rendering;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.network.messages.SyncPreviewCacheToClient;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import io.github.fabricators_of_create.porting_lib.entity.events.PlayerEvents;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Class handling blueprint syncing between players.
 */
public class ServerPreviewDistributor
{
    /**
     * Players that signed up to receive blueprint data.
     */
    private static Object2BooleanMap<UUID> registeredPlayers = new Object2BooleanOpenHashMap<>();

    public static void init() {
        PlayerEvents.LOGGED_OUT.register(player -> {
            onLogout(player);
        });
    }

    public static void onLogout(Player entity)
    {
        if (entity.level().isClientSide)
        {
            RenderingCache.clear();
            return;
        }
        registeredPlayers.removeBoolean(entity.getUUID());
    }

    /**
     * Distribute this rendering cache to all that are wanting to listen.
     * @param renderingCache the cache to distribute.
     */
    public static void distribute(final BlueprintPreviewData renderingCache, final ServerPlayer sourcePlayer)
    {
        for (final ServerPlayer player : sourcePlayer.getServer().getLevel(sourcePlayer.level().dimension()).players())
        {
            if ((player.blockPosition().distSqr(renderingCache.getPos()) < 128 * 128 || renderingCache.getPos().equals(BlockPos.ZERO)) && // within sensible distance
                !player.getUUID().equals(sourcePlayer.getUUID()) && // dont send to source
                player.isAlive() && // dont send to dead
                registeredPlayers.getBoolean(player.getUUID())) // only those who want to see previews
            {
                Network.getNetwork().sendToPlayer(new SyncPreviewCacheToClient(renderingCache, player.getUUID()), player);
            }
        }
    }

    /**
     * Register a player with their settings.
     * @param player the player.
     * @param displayShared if displayed is shared or not.
     */
    public static void register(final ServerPlayer player, final boolean displayShared)
    {
        registeredPlayers.put(player.getUUID(), displayShared);
    }
}
