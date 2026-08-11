package com.Theus452.walkietalkie.fabric.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.networking.packet.PacketCreateChannel;
import com.Theus452.walkietalkie.networking.packet.PacketJoinChannel;
import com.Theus452.walkietalkie.networking.packet.PacketRequestChannels;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketKickPlayer;
import com.Theus452.walkietalkie.networking.packet.PacketRenameChannel;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketBlockChannelAction;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class FabricPacketHandler {
    public static final ResourceLocation SET_FREQUENCY_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "set_frequency");
    public static final ResourceLocation CREATE_CHANNEL_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "create_channel");
    public static final ResourceLocation JOIN_CHANNEL_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "join_channel");
    public static final ResourceLocation REQUEST_CHANNELS_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "request_channels");
    public static final ResourceLocation PUSH_CHAT_MESSAGE_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "push_chat_message");
    public static final ResourceLocation SYNC_CHANNELS_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "sync_channels");
    public static final ResourceLocation CHANNEL_ACTION_RESULT_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "channel_action_result");
    public static final ResourceLocation KICK_PLAYER_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "kick_player");
    public static final ResourceLocation RENAME_CHANNEL_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "rename_channel");
    public static final ResourceLocation WALKIE_BLOCK_MESSAGE_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_block_message");
    public static final ResourceLocation SET_BLOCK_FREQUENCY_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "set_block_frequency");
    public static final ResourceLocation TOGGLE_BLOCK_RELAY_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "toggle_block_relay");
    public static final ResourceLocation BLOCK_CHANNEL_ACTION_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "block_channel_action");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SET_FREQUENCY_ID, (server, player, handler, buf, responseSender) -> {
            PacketSetFrequency packet = new PacketSetFrequency(buf);
            server.execute(() -> PacketSetFrequency.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(CREATE_CHANNEL_ID, (server, player, handler, buf, responseSender) -> {
            PacketCreateChannel packet = new PacketCreateChannel(buf);
            server.execute(() -> PacketCreateChannel.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(JOIN_CHANNEL_ID, (server, player, handler, buf, responseSender) -> {
            PacketJoinChannel packet = new PacketJoinChannel(buf);
            server.execute(() -> PacketJoinChannel.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_CHANNELS_ID, (server, player, handler, buf, responseSender) -> {
            PacketRequestChannels packet = new PacketRequestChannels(buf);
            server.execute(() -> PacketRequestChannels.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(KICK_PLAYER_ID, (server, player, handler, buf, responseSender) -> {
            PacketKickPlayer packet = new PacketKickPlayer(buf);
            server.execute(() -> PacketKickPlayer.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(RENAME_CHANNEL_ID, (server, player, handler, buf, responseSender) -> {
            PacketRenameChannel packet = new PacketRenameChannel(buf);
            server.execute(() -> PacketRenameChannel.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(WALKIE_BLOCK_MESSAGE_ID, (server, player, handler, buf, responseSender) -> {
            C2S_WalkieBlockMessagePacket packet = new C2S_WalkieBlockMessagePacket(buf);
            server.execute(() -> C2S_WalkieBlockMessagePacket.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_BLOCK_FREQUENCY_ID, (server, player, handler, buf, responseSender) -> {
            PacketSetBlockFrequency packet = new PacketSetBlockFrequency(buf);
            server.execute(() -> PacketSetBlockFrequency.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_BLOCK_RELAY_ID, (server, player, handler, buf, responseSender) -> {
            C2S_ToggleBlockRelayPacket packet = new C2S_ToggleBlockRelayPacket(buf);
            server.execute(() -> C2S_ToggleBlockRelayPacket.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(BLOCK_CHANNEL_ACTION_ID, (server, player, handler, buf, responseSender) -> {
            PacketBlockChannelAction packet = new PacketBlockChannelAction(buf);
            server.execute(() -> PacketBlockChannelAction.handle(packet, player));
        });
    }
}
