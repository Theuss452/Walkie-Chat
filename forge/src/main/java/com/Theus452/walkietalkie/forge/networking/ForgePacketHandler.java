package com.Theus452.walkietalkie.forge.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
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
    }
}