package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.util.ServerRateLimiter;
import com.Theus452.walkietalkie.util.WalkieSecurity;
import com.Theus452.walkietalkie.util.WalkieMessageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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

    public C2S_WalkieBlockMessagePacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.frequency = buf.readUtf(10);
        this.message = buf.readUtf(256);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos == null ? BlockPos.ZERO : pos);
        buf.writeUtf(frequency == null ? "" : frequency, 10);
        buf.writeUtf(message == null ? "" : message, 256);
    }

    public static void handle(C2S_WalkieBlockMessagePacket packet, ServerPlayer player) {
        if (!WalkieSecurity.isUsablePlayer(player)) return;
        String message = WalkieSecurity.sanitizeMessage(packet.message);
        if (message.isEmpty()) return;

        WalkieTalkieBlockEntity blockEntity = WalkieSecurity.interactableWalkieBlock(player, packet.pos);
        if (blockEntity == null || !blockEntity.isActive() || !blockEntity.isRelayEnabled()) return;
        if (!com.Theus452.walkietalkie.channel.ChannelManager.canAccess(player, blockEntity.getFrequency())) return;
        if (!ServerRateLimiter.allow(player, "walkietalkie:message_block", 5L)) return;
        WalkieMessageHelper.broadcastMessage(player.server, player, blockEntity.getFrequency(), message);
    }
}
