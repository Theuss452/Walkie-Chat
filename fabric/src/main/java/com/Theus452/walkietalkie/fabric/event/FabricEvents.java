package com.Theus452.walkietalkie.fabric.event;

import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
import com.Theus452.walkietalkie.util.ConnectionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FabricEvents {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ConnectionManager::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> WalkieBlockRegistry.clearOnStop());
    }
}
