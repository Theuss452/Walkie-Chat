package com.Theus452.walkietalkie.forge.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

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
        INSTANCE.registerMessage(id++, PacketSetFrequency.class, PacketSetFrequency::toBytes, PacketSetFrequency::new, ForgePacketHandler::handleSetFrequency);
        INSTANCE.registerMessage(id++, C2S_WalkieBlockMessagePacket.class, C2S_WalkieBlockMessagePacket::toBytes, C2S_WalkieBlockMessagePacket::new, ForgePacketHandler::handleWalkieBlockMessage);
        INSTANCE.registerMessage(id++, PacketSetBlockFrequency.class, PacketSetBlockFrequency::toBytes, PacketSetBlockFrequency::new, ForgePacketHandler::handleSetBlockFrequency);
        INSTANCE.registerMessage(id++, C2S_ToggleBlockRelayPacket.class, C2S_ToggleBlockRelayPacket::toBytes, C2S_ToggleBlockRelayPacket::new, ForgePacketHandler::handleToggleBlockRelay);
    }

    private static void handleSetFrequency(PacketSetFrequency packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            PacketSetFrequency.handle(packet, player);
        });
        context.setPacketHandled(true);
    }

    private static void handleWalkieBlockMessage(C2S_WalkieBlockMessagePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            C2S_WalkieBlockMessagePacket.handle(packet, player);
        });
        context.setPacketHandled(true);
    }

    private static void handleSetBlockFrequency(PacketSetBlockFrequency packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            PacketSetBlockFrequency.handle(packet, player);
        });
        context.setPacketHandled(true);
    }

    private static void handleToggleBlockRelay(C2S_ToggleBlockRelayPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            C2S_ToggleBlockRelayPacket.handle(packet, player);
        });
        context.setPacketHandled(true);
    }
}