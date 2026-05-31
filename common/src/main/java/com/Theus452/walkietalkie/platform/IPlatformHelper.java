package com.Theus452.walkietalkie.platform;

public interface IPlatformHelper {
    
    void sendToServer(Object packet);
    void sendToClient(Object packet, net.minecraft.server.level.ServerPlayer player);
    double getChatRange();
    boolean isModLoaded(String modId);
}
