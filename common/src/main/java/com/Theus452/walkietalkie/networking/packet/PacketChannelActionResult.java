package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.client.ChannelActionCache;
import net.minecraft.network.FriendlyByteBuf;

public final class PacketChannelActionResult {
    private final boolean success;
    private final String frequency;
    private final String messageKey;
    private final ChannelActionType actionType;
    private final long requestId;

    public PacketChannelActionResult(boolean success, String frequency, String messageKey, ChannelActionType actionType, long requestId) {
        this.success = success;
        this.frequency = frequency;
        this.messageKey = messageKey;
        this.actionType = actionType;
        this.requestId = requestId;
    }

    public PacketChannelActionResult(FriendlyByteBuf buffer) {
        success = buffer.readBoolean();
        frequency = buffer.readUtf(3);
        messageKey = buffer.readUtf(64);
        actionType = buffer.readEnum(ChannelActionType.class);
        requestId = buffer.readVarLong();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(success);
        buffer.writeUtf(frequency, 3);
        buffer.writeUtf(messageKey, 64);
        buffer.writeEnum(actionType);
        buffer.writeVarLong(requestId);
    }

    public static void handle(PacketChannelActionResult packet) {
        ChannelActionCache.set(packet.success, packet.frequency, packet.messageKey, packet.actionType, packet.requestId);
    }
}
