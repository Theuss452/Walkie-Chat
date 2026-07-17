package com.Theus452.walkietalkie.neoforge.platform;

import com.Theus452.walkietalkie.neoforge.config.NeoForgeModConfigs;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public void sendToServer(Object packet) {
        if (packet instanceof PacketSetFrequency p) {
            PacketDistributor.sendToServer(new PacketSetFrequencyPayload(p.getNewFrequency(), p.getHand()));
        }
    }

    @Override
    public double getChatRange() {
        return NeoForgeModConfigs.CHAT_RANGE.get();
    }
}
