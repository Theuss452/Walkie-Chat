package com.Theus452.walkietalkie.networking.packet;

import net.minecraft.network.FriendlyByteBuf;

public class PacketPushChatMessage {
    private final String frequency;
    private final String senderName;
    private final String message;

    public PacketPushChatMessage(String frequency, String senderName, String message) {
        this.frequency  = frequency;
        this.senderName = senderName;
        this.message    = message;
    }

    public PacketPushChatMessage(FriendlyByteBuf buf) {
        this.frequency  = buf.readUtf(10);
        this.senderName = buf.readUtf(64);
        this.message    = buf.readUtf(256);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(frequency, 10);
        buf.writeUtf(senderName, 64);
        buf.writeUtf(message, 256);
    }

    public static void handle(PacketPushChatMessage pkt) {
        com.Theus452.walkietalkie.client.ChannelMessageCache.add(pkt.frequency, pkt.senderName, pkt.message);
    }

    public String frequency()  { return frequency; }
    public String senderName() { return senderName; }
    public String message()    { return message; }
}
