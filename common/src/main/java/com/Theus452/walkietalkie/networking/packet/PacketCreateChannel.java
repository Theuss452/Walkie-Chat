package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.channel.ChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public final class PacketCreateChannel {
    private final String frequency;
    private final String name;
    private final boolean passwordProtected;
    private final String password;
    private final InteractionHand hand;
    private final long requestId;

    public PacketCreateChannel(
            String frequency,
            String name,
            boolean passwordProtected,
            String password,
            InteractionHand hand,
            long requestId
    ) {
        this.frequency = frequency;
        this.name = name;
        this.passwordProtected = passwordProtected;
        this.password = password;
        this.hand = hand;
        this.requestId = requestId;
    }

    public PacketCreateChannel(FriendlyByteBuf buffer) {
        frequency = buffer.readUtf(3);
        name = buffer.readUtf(24);
        passwordProtected = buffer.readBoolean();
        password = buffer.readUtf(32);
        hand = buffer.readEnum(InteractionHand.class);
        requestId = buffer.readVarLong();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(frequency, 3);
        buffer.writeUtf(name, 24);
        buffer.writeBoolean(passwordProtected);
        buffer.writeUtf(password, 32);
        buffer.writeEnum(hand);
        buffer.writeVarLong(requestId);
    }

    public static void handle(PacketCreateChannel packet, ServerPlayer player) {
        ChannelManager.createChannel(
                player,
                packet.hand,
                packet.frequency,
                packet.name,
                packet.passwordProtected,
                packet.password,
                packet.requestId
        );
    }
}
