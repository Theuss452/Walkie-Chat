package com.Theus452.walkietalkie.fabric.event;

import com.Theus452.walkietalkie.util.ConnectionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FabricEvents {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ConnectionManager::tick);
    }
}