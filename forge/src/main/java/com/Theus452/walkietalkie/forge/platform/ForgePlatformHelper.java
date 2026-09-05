package com.Theus452.walkietalkie.forge.platform;

import com.Theus452.walkietalkie.forge.config.ForgeModConfigs;
import com.Theus452.walkietalkie.forge.networking.ForgePacketHandler;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPayload;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequencyPayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public void sendToServer(Object packet) {
        if (packet instanceof PacketSetFrequency p) {
            ForgePacketHandler.CHANNEL.send(new PacketSetFrequencyPayload(p.getNewFrequency(), p.getHand()), PacketDistributor.SERVER.noArg());
        } else if (packet instanceof PacketSetBlockFrequency p) {
            ForgePacketHandler.CHANNEL.send(new PacketSetBlockFrequencyPayload(p.getFrequency(), p.getPos()), PacketDistributor.SERVER.noArg());
        } else if (packet instanceof C2S_ToggleBlockRelayPacket p) {
            ForgePacketHandler.CHANNEL.send(new C2S_ToggleBlockRelayPayload(p.getPos(), p.isRelayEnabled()), PacketDistributor.SERVER.noArg());
        } else if (packet instanceof C2S_WalkieBlockMessagePacket p) {
            ForgePacketHandler.CHANNEL.send(new C2S_WalkieBlockMessagePayload(p.getPos(), p.getFrequency(), p.getMessage()), PacketDistributor.SERVER.noArg());
        }
    }

    @Override
    public double getChatRange() {
        return ForgeModConfigs.CHAT_RANGE.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}