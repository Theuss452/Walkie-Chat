package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.channel.ChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public final class PacketSetFrequency {
    private final String newFrequency;
    private final InteractionHand hand;
    private final long requestId;

    public PacketSetFrequency(String frequency, InteractionHand hand, long requestId) {
        newFrequency = frequency;
        this.hand = hand;
        this.requestId = requestId;
    }

    public PacketSetFrequency(FriendlyByteBuf buffer) {
        newFrequency = buffer.readUtf(3);
        hand = buffer.readEnum(InteractionHand.class);
        requestId = buffer.readVarLong();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(newFrequency, 3);
        buffer.writeEnum(hand);
        buffer.writeVarLong(requestId);
    }

    public static void handle(PacketSetFrequency packet, ServerPlayer player) {
        ChannelManager.setFrequency(player, packet.hand, packet.newFrequency, packet.requestId);
    }
}
