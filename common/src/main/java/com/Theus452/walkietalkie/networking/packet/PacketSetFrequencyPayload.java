package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record PacketSetFrequencyPayload(String frequency, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<PacketSetFrequencyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "set_frequency"));

    public static final StreamCodec<FriendlyByteBuf, PacketSetFrequencyPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.frequency());
                buf.writeEnum(payload.hand());
            },
            buf -> new PacketSetFrequencyPayload(buf.readUtf(), buf.readEnum(InteractionHand.class))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
