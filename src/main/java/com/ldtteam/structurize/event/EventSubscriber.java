package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.IOPool;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Class with methods for receiving various forge events
 */
public class EventSubscriber
{
    /**
     * Private constructor to hide implicit public one.
     */
    private EventSubscriber()
    {
        /*
         * Intentionally left empty
         */
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            onRegisterCommands(dispatcher, environment);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            onPlayerLogin(handler.getPlayer());
        });

        ClientTickEvents.START_WORLD_TICK.register(world -> {
            onWorldTick(world);
        });

        ServerTickEvents.START_WORLD_TICK.register(world -> {
            onWorldTick(world);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            onServerStopped();
        });
    }

    /**
     * Called when world is about to load.
     *
     */
    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection selection)
    {
        EntryPoint.register(dispatcher, selection);
    }

    /**
     * Called when a player logs in. If the joining player is a MP-Player, sends
     * all possible styles in a message.
     *
     */
    public static void onPlayerLogin(ServerPlayer serverPlayer)
    {
        //if (event.getEntity() instanceof ServerPlayer serverPlayer)
        //{
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), serverPlayer);
        //}
    }

    public static void onWorldTick(Level level)
    {
        if (level instanceof ServerLevel serverLevel)
        {
            //if (event.phase == Phase.START)
            //{
                BlockUtils.checkOrInit();
                Manager.onWorldTick(serverLevel);
            //}
        }
        else if (level instanceof ClientLevel)
        {
            BlockUtils.checkOrInit();
        }
    }

    public static void onServerStopped()
    {
        IOPool.shutdown();
    }
}
