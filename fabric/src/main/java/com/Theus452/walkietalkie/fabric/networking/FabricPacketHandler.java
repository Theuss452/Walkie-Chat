package com.Theus452.walkietalkie.fabric.networking;

import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class FabricPacketHandler {

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(PacketSetFrequencyPayload.TYPE, PacketSetFrequencyPayload.CODEC);
    }

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PacketSetFrequencyPayload.TYPE, (payload, context) -> {
            PacketSetFrequency packet = new PacketSetFrequency(payload.frequency(), payload.hand());
            context.server().execute(() -> PacketSetFrequency.handle(packet, context.player()));
        });
    }
}