package com.Theus452.walkietalkie.fabric.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class FabricPacketHandler {
    public static final ResourceLocation SET_FREQUENCY_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "set_frequency");
    public static final ResourceLocation WALKIE_BLOCK_MESSAGE_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_block_message");
    public static final ResourceLocation SET_BLOCK_FREQUENCY_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "set_block_frequency");
    public static final ResourceLocation TOGGLE_BLOCK_RELAY_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "toggle_block_relay");
    public static final ResourceLocation PUSH_CHAT_MESSAGE_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "push_chat_message");
    public static final ResourceLocation SYNC_CHANNELS_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "sync_channels");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SET_FREQUENCY_ID, (server, player, handler, buf, responseSender) -> {
            PacketSetFrequency packet = new PacketSetFrequency(buf);
            server.execute(() -> PacketSetFrequency.handle(packet, player));
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
    }
}
