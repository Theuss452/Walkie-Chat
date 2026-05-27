package com.Theus452.walkietalkie.forge.platform;

import com.Theus452.walkietalkie.forge.config.ForgeModConfigs;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import com.Theus452.walkietalkie.forge.networking.ForgePacketHandler;
import net.minecraftforge.network.PacketDistributor;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public void sendToServer(Object packet) {
        if (packet instanceof PacketSetFrequency) {
            PacketSetFrequency p = (PacketSetFrequency) packet;
            ForgePacketHandler.CHANNEL.send(new PacketSetFrequencyPayload(p.getNewFrequency(), p.getHand()), PacketDistributor.SERVER.noArg());
        }
    }

    @Override
    public double getChatRange() {
        return ForgeModConfigs.CHAT_RANGE.get();
    }
}