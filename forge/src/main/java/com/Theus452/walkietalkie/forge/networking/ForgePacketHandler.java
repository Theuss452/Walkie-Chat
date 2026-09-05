package com.Theus452.walkietalkie.forge.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPayload;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequencyPayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class ForgePacketHandler {

    public static SimpleChannel CHANNEL;

    @SuppressWarnings("unchecked")
    public static void register() {
        CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "main"))
                .networkProtocolVersion(1)
                .acceptedVersions((status, version) -> true)
                .simpleChannel();

        CHANNEL.messageBuilder(PacketSetFrequencyPayload.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .codec((StreamCodec<RegistryFriendlyByteBuf, PacketSetFrequencyPayload>) (StreamCodec<?, ?>) PacketSetFrequencyPayload.CODEC)
                .consumerMainThread((payload, context) -> {
                    ServerPlayer player = context.getSender();
                    if (player != null) {
                        PacketSetFrequency packet = new PacketSetFrequency(payload.frequency(), payload.hand());
                        PacketSetFrequency.handle(packet, player);
                    }
                    context.setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(PacketSetBlockFrequencyPayload.class, 2, NetworkDirection.PLAY_TO_SERVER)
                .codec((StreamCodec<RegistryFriendlyByteBuf, PacketSetBlockFrequencyPayload>) (StreamCodec<?, ?>) PacketSetBlockFrequencyPayload.CODEC)
                .consumerMainThread((payload, context) -> {
                    ServerPlayer player = context.getSender();
                    if (player != null) {
                        PacketSetBlockFrequency packet = new PacketSetBlockFrequency(payload.frequency(), payload.pos());
                        PacketSetBlockFrequency.handle(packet, player);
                    }
                    context.setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(C2S_ToggleBlockRelayPayload.class, 3, NetworkDirection.PLAY_TO_SERVER)
                .codec((StreamCodec<RegistryFriendlyByteBuf, C2S_ToggleBlockRelayPayload>) (StreamCodec<?, ?>) C2S_ToggleBlockRelayPayload.CODEC)
                .consumerMainThread((payload, context) -> {
                    ServerPlayer player = context.getSender();
                    if (player != null) {
                        C2S_ToggleBlockRelayPacket packet = new C2S_ToggleBlockRelayPacket(payload.pos(), payload.relayEnabled());
                        C2S_ToggleBlockRelayPacket.handle(packet, player);
                    }
                    context.setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(C2S_WalkieBlockMessagePayload.class, 4, NetworkDirection.PLAY_TO_SERVER)
                .codec((StreamCodec<RegistryFriendlyByteBuf, C2S_WalkieBlockMessagePayload>) (StreamCodec<?, ?>) C2S_WalkieBlockMessagePayload.CODEC)
                .consumerMainThread((payload, context) -> {
                    ServerPlayer player = context.getSender();
                    if (player != null) {
                        C2S_WalkieBlockMessagePacket packet = new C2S_WalkieBlockMessagePacket(payload.pos(), payload.frequency(), payload.message());
                        C2S_WalkieBlockMessagePacket.handle(packet, player);
                    }
                    context.setPacketHandled(true);
                })
                .add();
    }
}