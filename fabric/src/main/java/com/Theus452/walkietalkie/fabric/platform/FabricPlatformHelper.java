package com.Theus452.walkietalkie.fabric.platform;

import com.Theus452.walkietalkie.fabric.config.FabricModConfigs;
import com.Theus452.walkietalkie.fabric.networking.FabricPacketHandler;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public void sendToServer(Object packet) {
        if (packet instanceof PacketSetFrequency p) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            p.toBytes(buf);
            ClientPlayNetworking.send(FabricPacketHandler.SET_FREQUENCY_ID, buf);
        } else if (packet instanceof PacketSetBlockFrequency p) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            p.toBytes(buf);
            ClientPlayNetworking.send(FabricPacketHandler.SET_BLOCK_FREQUENCY_ID, buf);
        }
    }

    @Override
    public void sendToClient(Object packet, net.minecraft.server.level.ServerPlayer player) {
        if (packet instanceof com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage p) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            p.toBytes(buf);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, FabricPacketHandler.PUSH_CHAT_MESSAGE_ID, buf);
        } else if (packet instanceof com.Theus452.walkietalkie.networking.packet.PacketSyncChannels p) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            p.toBytes(buf);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, FabricPacketHandler.SYNC_CHANNELS_ID, buf);
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
