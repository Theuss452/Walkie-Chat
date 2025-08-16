package com.theus452.walkietalkie.forge.platform;

import com.theus452.walkietalkie.forge.config.ForgeModConfigs;
import com.theus452.walkietalkie.forge.networking.ForgePacketHandler;
import com.theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.theus452.walkietalkie.platform.IPlatformHelper;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public void sendToServer(Object packet) {
        if (packet instanceof PacketSetFrequency) {
            ForgePacketHandler.INSTANCE.sendToServer(packet);
        }
    }

    @Override
    public double getChatRange() {
        return ForgeModConfigs.CHAT_RANGE.get();
    }
}