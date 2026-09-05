package com.Theus452.walkietalkie.fabric.platform;

import com.Theus452.walkietalkie.fabric.config.FabricModConfigs;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPayload;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequencyPayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public void sendToServer(Object packet) {
        if (packet instanceof PacketSetFrequency p) {
            ClientPlayNetworking.send(new PacketSetFrequencyPayload(p.getNewFrequency(), p.getHand()));
        } else if (packet instanceof PacketSetBlockFrequency p) {
            ClientPlayNetworking.send(new PacketSetBlockFrequencyPayload(p.getFrequency(), p.getPos()));
        } else if (packet instanceof C2S_ToggleBlockRelayPacket p) {
            ClientPlayNetworking.send(new C2S_ToggleBlockRelayPayload(p.getPos(), p.isRelayEnabled()));
        } else if (packet instanceof C2S_WalkieBlockMessagePacket p) {
            ClientPlayNetworking.send(new C2S_WalkieBlockMessagePayload(p.getPos(), p.getFrequency(), p.getMessage()));
        }
    }

    @Override
    public double getChatRange() {
        return FabricModConfigs.getChatRange();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}