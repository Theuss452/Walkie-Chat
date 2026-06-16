package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.util.ConnectionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class PacketRequestChannels {
    public PacketRequestChannels() {
    }

    public PacketRequestChannels(FriendlyByteBuf buffer) {
    }

    public void toBytes(FriendlyByteBuf buffer) {
    }

    public static void handle(PacketRequestChannels packet, ServerPlayer player) {
        ConnectionManager.syncActiveChannels(player.server, player, true);
    }
}
