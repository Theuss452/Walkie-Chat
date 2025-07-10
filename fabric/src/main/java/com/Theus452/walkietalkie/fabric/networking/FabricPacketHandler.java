package com.Theus452.walkietalkie.fabric.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class FabricPacketHandler {
    public static final ResourceLocation SET_FREQUENCY_ID = new ResourceLocation(WalkieTalkieMod.MOD_ID, "set_frequency");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SET_FREQUENCY_ID, (server, player, handler, buf, responseSender) -> {
            PacketSetFrequency packet = new PacketSetFrequency(buf);

            server.execute(() -> PacketSetFrequency.handle(packet, player));
        });
    }
}