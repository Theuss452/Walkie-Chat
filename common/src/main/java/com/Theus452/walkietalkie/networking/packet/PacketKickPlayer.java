package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.channel.ChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class PacketKickPlayer {
    private final String frequency;
    private final String playerName;

    public PacketKickPlayer(String frequency, String playerName) {
        this.frequency = frequency;
        this.playerName = playerName;
    }

    public PacketKickPlayer(FriendlyByteBuf buffer) {
        frequency = buffer.readUtf(3);
        playerName = buffer.readUtf(16);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(frequency, 3);
        buffer.writeUtf(playerName, 16);
    }

    public static void handle(PacketKickPlayer packet, ServerPlayer player) {
        ChannelManager.kickPlayerFromFrequency(player, packet.frequency, packet.playerName);
    }
}
