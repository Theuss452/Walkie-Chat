package com.Theus452.walkietalkie.neoforge.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
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
    }
}
