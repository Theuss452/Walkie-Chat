package com.theus452.walkietalkie.forge.networking;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.networking.packet.PacketSetFrequency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ForgePacketHandler {
    private static final String PROTOCOL_VERSION = "1";


    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WalkieTalkieMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, PacketSetFrequency.class, PacketSetFrequency::toBytes, PacketSetFrequency::new, (packet, context) -> {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (player != null) {
                    PacketSetFrequency.handle(packet, player);
                }
            });
            context.get().setPacketHandled(true);
        });
    }
}