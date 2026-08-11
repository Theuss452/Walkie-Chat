package com.Theus452.walkietalkie.fabric.platform;

import com.Theus452.walkietalkie.fabric.config.FabricModConfigs;
import com.Theus452.walkietalkie.fabric.networking.FabricPacketHandler;
import com.Theus452.walkietalkie.networking.packet.PacketChannelActionResult;
import com.Theus452.walkietalkie.networking.packet.PacketCreateChannel;
import com.Theus452.walkietalkie.networking.packet.PacketJoinChannel;
import com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage;
import com.Theus452.walkietalkie.networking.packet.PacketRequestChannels;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;
import com.Theus452.walkietalkie.networking.packet.PacketKickPlayer;
import com.Theus452.walkietalkie.networking.packet.PacketRenameChannel;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.PacketBlockChannelAction;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public void sendToServer(Object packet) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        if (packet instanceof PacketSetFrequency value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.SET_FREQUENCY_ID, buffer);
        } else if (packet instanceof PacketCreateChannel value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.CREATE_CHANNEL_ID, buffer);
        } else if (packet instanceof PacketJoinChannel value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.JOIN_CHANNEL_ID, buffer);
        } else if (packet instanceof PacketRequestChannels value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.REQUEST_CHANNELS_ID, buffer);
        } else if (packet instanceof PacketKickPlayer value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.KICK_PLAYER_ID, buffer);
        } else if (packet instanceof PacketRenameChannel value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.RENAME_CHANNEL_ID, buffer);
        } else if (packet instanceof PacketSetBlockFrequency value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.SET_BLOCK_FREQUENCY_ID, buffer);
        } else if (packet instanceof C2S_WalkieBlockMessagePacket value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.WALKIE_BLOCK_MESSAGE_ID, buffer);
        } else if (packet instanceof PacketBlockChannelAction value) {
            value.toBytes(buffer);
            ClientPlayNetworking.send(FabricPacketHandler.BLOCK_CHANNEL_ACTION_ID, buffer);
        }
    }

    @Override
    public void sendToClient(Object packet, ServerPlayer player) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        if (packet instanceof PacketPushChatMessage value) {
            value.toBytes(buffer);
            ServerPlayNetworking.send(player, FabricPacketHandler.PUSH_CHAT_MESSAGE_ID, buffer);
        } else if (packet instanceof PacketSyncChannels value) {
            value.toBytes(buffer);
            ServerPlayNetworking.send(player, FabricPacketHandler.SYNC_CHANNELS_ID, buffer);
        } else if (packet instanceof PacketChannelActionResult value) {
            value.toBytes(buffer);
            ServerPlayNetworking.send(player, FabricPacketHandler.CHANNEL_ACTION_RESULT_ID, buffer);
        }
    }

    @Override
    public double getChatRange() {
        return FabricModConfigs.getChatRange();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
    }
}
