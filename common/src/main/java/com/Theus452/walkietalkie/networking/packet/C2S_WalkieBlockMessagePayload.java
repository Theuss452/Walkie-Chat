package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2S_WalkieBlockMessagePayload(BlockPos pos, String frequency, String message) implements CustomPacketPayload {
    public static final Type<C2S_WalkieBlockMessagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "walkie_block_message"));

    public static final StreamCodec<FriendlyByteBuf, C2S_WalkieBlockMessagePayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeUtf(payload.frequency(), 10);
                buf.writeUtf(payload.message(), 256);
            },
            buf -> new C2S_WalkieBlockMessagePayload(buf.readBlockPos(), buf.readUtf(10), buf.readUtf(256))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
