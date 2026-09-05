package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacketSetBlockFrequencyPayload(String frequency, BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketSetBlockFrequencyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "set_block_frequency"));

    public static final StreamCodec<FriendlyByteBuf, PacketSetBlockFrequencyPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.frequency(), 10);
                buf.writeBlockPos(payload.pos());
            },
            buf -> new PacketSetBlockFrequencyPayload(buf.readUtf(10), buf.readBlockPos())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
