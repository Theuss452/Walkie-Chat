package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.channel.ChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public final class PacketJoinChannel {
    private final String frequency;
    private final String password;
    private final InteractionHand hand;
    private final long requestId;

    public PacketJoinChannel(String frequency, String password, InteractionHand hand, long requestId) {
        this.frequency = frequency;
        this.password = password;
        this.hand = hand;
        this.requestId = requestId;
    }

    public PacketJoinChannel(FriendlyByteBuf buffer) {
        frequency = buffer.readUtf(3);
        password = buffer.readUtf(32);
        hand = buffer.readEnum(InteractionHand.class);
        requestId = buffer.readVarLong();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(frequency, 3);
        buffer.writeUtf(password, 32);
        buffer.writeEnum(hand);
        buffer.writeVarLong(requestId);
    }

    public static void handle(PacketJoinChannel packet, ServerPlayer player) {
        ChannelManager.joinChannel(player, packet.hand, packet.frequency, packet.password, packet.requestId);
    }
}
