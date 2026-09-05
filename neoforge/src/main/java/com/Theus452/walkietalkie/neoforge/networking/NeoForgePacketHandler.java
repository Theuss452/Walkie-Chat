package com.Theus452.walkietalkie.neoforge.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPayload;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequencyPayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoForgePacketHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(WalkieTalkieMod.MOD_ID);

        registrar.playToServer(
                PacketSetFrequencyPayload.TYPE,
                PacketSetFrequencyPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer player) {
                            PacketSetFrequency packet = new PacketSetFrequency(payload.frequency(), payload.hand());
                            PacketSetFrequency.handle(packet, player);
                        }
                    });
                }
        );

        registrar.playToServer(
                PacketSetBlockFrequencyPayload.TYPE,
                PacketSetBlockFrequencyPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer player) {
                            PacketSetBlockFrequency packet = new PacketSetBlockFrequency(payload.frequency(), payload.pos());
                            PacketSetBlockFrequency.handle(packet, player);
                        }
                    });
                }
        );

        registrar.playToServer(
                C2S_ToggleBlockRelayPayload.TYPE,
                C2S_ToggleBlockRelayPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer player) {
                            C2S_ToggleBlockRelayPacket packet = new C2S_ToggleBlockRelayPacket(payload.pos(), payload.relayEnabled());
                            C2S_ToggleBlockRelayPacket.handle(packet, player);
                        }
                    });
                }
        );

        registrar.playToServer(
                C2S_WalkieBlockMessagePayload.TYPE,
                C2S_WalkieBlockMessagePayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer player) {
                            C2S_WalkieBlockMessagePacket packet = new C2S_WalkieBlockMessagePacket(payload.pos(), payload.frequency(), payload.message());
                            C2S_WalkieBlockMessagePacket.handle(packet, player);
                        }
                    });
                }
        );
    }
}
