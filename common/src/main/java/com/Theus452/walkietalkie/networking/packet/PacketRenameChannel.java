package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.channel.ChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class PacketRenameChannel {
    private final String frequency;
    private final String newName;

    public PacketRenameChannel(String frequency, String newName) {
        this.frequency = frequency;
        this.newName = newName;
    }

    public PacketRenameChannel(FriendlyByteBuf buffer) {
        frequency = buffer.readUtf(3);
        newName = buffer.readUtf(16);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(frequency, 3);
        buffer.writeUtf(newName, 16);
    }

    public static void handle(PacketRenameChannel packet, ServerPlayer player) {
        ChannelManager.renameChannel(player.server, player, packet.frequency, packet.newName);
    }
}
