package com.Theus452.walkietalkie.neoforge.platform;

import com.Theus452.walkietalkie.neoforge.config.NeoForgeModConfigs;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPayload;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequencyPayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public void sendToServer(Object packet) {
        if (packet instanceof PacketSetFrequency p) {
            PacketDistributor.sendToServer(new PacketSetFrequencyPayload(p.getNewFrequency(), p.getHand()));
        } else if (packet instanceof PacketSetBlockFrequency p) {
            PacketDistributor.sendToServer(new PacketSetBlockFrequencyPayload(p.getFrequency(), p.getPos()));
        } else if (packet instanceof C2S_ToggleBlockRelayPacket p) {
            PacketDistributor.sendToServer(new C2S_ToggleBlockRelayPayload(p.getPos(), p.isRelayEnabled()));
        } else if (packet instanceof C2S_WalkieBlockMessagePacket p) {
            PacketDistributor.sendToServer(new C2S_WalkieBlockMessagePayload(p.getPos(), p.getFrequency(), p.getMessage()));
        }
    }

    @Override
    public double getChatRange() {
        return NeoForgeModConfigs.CHAT_RANGE.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
