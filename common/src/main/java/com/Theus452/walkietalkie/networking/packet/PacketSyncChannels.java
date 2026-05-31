package com.Theus452.walkietalkie.networking.packet;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketSyncChannels {
    public static class ChannelInfo {
        private final String frequency;
        private final int playerCount;

        public ChannelInfo(String frequency, int playerCount) {
            this.frequency = frequency;
            this.playerCount = playerCount;
        }

        public String frequency() { return frequency; }
        public int playerCount() { return playerCount; }
    }

    private final List<ChannelInfo> channels;
    public PacketSyncChannels(List<ChannelInfo> channels) {
        this.channels = Collections.unmodifiableList(new ArrayList<>(channels));
    }

    public PacketSyncChannels(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ChannelInfo> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String freq = buf.readUtf(10);
            int count = buf.readVarInt();
            list.add(new ChannelInfo(freq, count));
        }
        this.channels = Collections.unmodifiableList(list);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(channels.size());
        for (ChannelInfo ch : channels) {
            buf.writeUtf(ch.frequency(), 10);
            buf.writeVarInt(ch.playerCount());
        }
    }

    public List<ChannelInfo> channels() { return channels; }

    public static void handle(PacketSyncChannels pkt) {
        com.Theus452.walkietalkie.client.ChannelCache.set(pkt.channels());
    }
}
