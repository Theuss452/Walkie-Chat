package com.Theus452.walkietalkie.networking.packet;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketSyncChannels {
    public static class ChannelInfo {
        private final String frequency;
        private final List<String> players;

        public ChannelInfo(String frequency, List<String> players) {
            this.frequency = frequency;
            this.players = Collections.unmodifiableList(new ArrayList<>(players));
        }

        public String frequency() { return frequency; }
        public int playerCount() { return players.size(); }
        public List<String> players() { return players; }
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
            int pSize = buf.readVarInt();
            List<String> players = new ArrayList<>(pSize);
            for (int j = 0; j < pSize; j++) {
                players.add(buf.readUtf(32));
            }
            list.add(new ChannelInfo(freq, players));
        }
        this.channels = Collections.unmodifiableList(list);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(channels.size());
        for (ChannelInfo ch : channels) {
            buf.writeUtf(ch.frequency(), 10);
            buf.writeVarInt(ch.players().size());
            for (String p : ch.players()) {
                buf.writeUtf(p, 32);
            }
        }
    }

    public List<ChannelInfo> channels() { return channels; }

    public static void handle(PacketSyncChannels pkt) {
        com.Theus452.walkietalkie.client.ChannelCache.set(pkt.channels());
    }
}
