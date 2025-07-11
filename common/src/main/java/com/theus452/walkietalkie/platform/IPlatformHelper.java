package com.theus452.walkietalkie.platform;

public interface IPlatformHelper {
    
    void sendToServer(Object packet);
    double getChatRange();
}