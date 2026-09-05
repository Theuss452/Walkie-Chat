package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2S_ToggleBlockRelayPayload(BlockPos pos, boolean relayEnabled) implements CustomPacketPayload {
    public static final Type<C2S_ToggleBlockRelayPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "toggle_block_relay"));

    public static final StreamCodec<FriendlyByteBuf, C2S_ToggleBlockRelayPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeBoolean(payload.relayEnabled());
            },
            buf -> new C2S_ToggleBlockRelayPayload(buf.readBlockPos(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
