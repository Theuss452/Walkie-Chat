package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.util.ServerRateLimiter;
import com.Theus452.walkietalkie.util.WalkieSecurity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class C2S_ToggleBlockRelayPacket {
    private static final long TOGGLE_INTERVAL_TICKS = 5L;

    private final BlockPos pos;
    private final boolean relayEnabled;

    public C2S_ToggleBlockRelayPacket(BlockPos pos, boolean relayEnabled) {
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.relayEnabled = relayEnabled;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isRelayEnabled() {
        return relayEnabled;
    }

    public static void handle(C2S_ToggleBlockRelayPacket packet, ServerPlayer player) {
        if (!ServerRateLimiter.allow(player, "walkietalkie:block_relay", TOGGLE_INTERVAL_TICKS)) return;
        WalkieTalkieBlockEntity blockEntity = WalkieSecurity.interactableWalkieBlock(player, packet.pos);
        if (blockEntity == null) return;
        if (!WalkieSecurity.canControlWalkieBlock(player, blockEntity)) return;
        blockEntity.setRelayEnabled(packet.relayEnabled);
        if (blockEntity.ownerUUID == null) {
            blockEntity.setOwnerUUID(player.getUUID());
        }
    }
}
