package com.ldtteam.structurize.fabric;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import io.github.fabricators_of_create.porting_lib.util.ServerLifecycleHooks;
import net.minecraft.client.Minecraft;

public class NetworkHelper {
    public static void enqueueWork(boolean onServer, Runnable runnable) {
        EnvExecutor.unsafeRunForDist(() -> () -> {
            if (onServer)
                ServerLifecycleHooks.getCurrentServer().execute(runnable);
            else
                Minecraft.getInstance().execute(runnable);
            return null;
        }, () -> () -> {
            ServerLifecycleHooks.getCurrentServer().execute(runnable);
            return null;
        });
    }
}
