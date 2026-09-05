package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.util.ServerRateLimiter;
import com.Theus452.walkietalkie.util.WalkieFrequency;
import com.Theus452.walkietalkie.util.WalkieSecurity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class C2S_WalkieBlockMessagePacket {
    private final BlockPos pos;
    private final String frequency;
    private final String message;

    public C2S_WalkieBlockMessagePacket(BlockPos pos, String frequency, String message) {
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.frequency = frequency == null ? "" : frequency;
        this.message = message == null ? "" : message;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getMessage() {
        return message;
    }

    public static void handle(C2S_WalkieBlockMessagePacket packet, ServerPlayer player) {
        if (!WalkieSecurity.isUsablePlayer(player)) return;
        String frequency = WalkieSecurity.sanitizeFrequency(packet.frequency);
        String message = WalkieSecurity.sanitizeMessage(packet.message);
        if (frequency.isEmpty() || message.isEmpty()) return;
        if (WalkieFrequency.isPrivate(frequency)) return;

        WalkieTalkieBlockEntity blockEntity = WalkieSecurity.interactableWalkieBlock(player, packet.pos);
        if (blockEntity == null || !blockEntity.isActive() || !blockEntity.isRelayEnabled()) return;
        if (!frequency.equals(blockEntity.getFrequency())) return;
        if (!ServerRateLimiter.allow(player, "walkietalkie:message_block", 5L)) return;
    }
}
