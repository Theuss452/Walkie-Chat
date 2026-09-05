package com.Theus452.walkietalkie.fabric.networking;

import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPayload;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequencyPayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class FabricPacketHandler {

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(PacketSetFrequencyPayload.TYPE, PacketSetFrequencyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PacketSetBlockFrequencyPayload.TYPE, PacketSetBlockFrequencyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_ToggleBlockRelayPayload.TYPE, C2S_ToggleBlockRelayPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_WalkieBlockMessagePayload.TYPE, C2S_WalkieBlockMessagePayload.CODEC);
    }

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PacketSetFrequencyPayload.TYPE, (payload, context) -> {
            PacketSetFrequency packet = new PacketSetFrequency(payload.frequency(), payload.hand());
            context.server().execute(() -> PacketSetFrequency.handle(packet, context.player()));
        });

        ServerPlayNetworking.registerGlobalReceiver(PacketSetBlockFrequencyPayload.TYPE, (payload, context) -> {
            PacketSetBlockFrequency packet = new PacketSetBlockFrequency(payload.frequency(), payload.pos());
            context.server().execute(() -> PacketSetBlockFrequency.handle(packet, context.player()));
        });

        ServerPlayNetworking.registerGlobalReceiver(C2S_ToggleBlockRelayPayload.TYPE, (payload, context) -> {
            C2S_ToggleBlockRelayPacket packet = new C2S_ToggleBlockRelayPacket(payload.pos(), payload.relayEnabled());
            context.server().execute(() -> C2S_ToggleBlockRelayPacket.handle(packet, context.player()));
        });

        ServerPlayNetworking.registerGlobalReceiver(C2S_WalkieBlockMessagePayload.TYPE, (payload, context) -> {
            C2S_WalkieBlockMessagePacket packet = new C2S_WalkieBlockMessagePacket(payload.pos(), payload.frequency(), payload.message());
            context.server().execute(() -> C2S_WalkieBlockMessagePacket.handle(packet, context.player()));
        });
    }
}