package com.Theus452.walkietalkie.networking.packet;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PacketSyncChannels {
    public record ChannelInfo(
            String frequency,
            String name,
            String ownerName,
            int playerCount,
            List<String> players,
            boolean passwordProtected,
            boolean persistent,
            int userBlockCount
    ) {
        public ChannelInfo {
            players = Collections.unmodifiableList(new ArrayList<>(players));
        }
    }

    private final List<ChannelInfo> channels;

    public PacketSyncChannels(List<ChannelInfo> channels) {
        this.channels = Collections.unmodifiableList(new ArrayList<>(channels));
    }

    public PacketSyncChannels(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ChannelInfo> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String freq = buf.readUtf(16);
            String name = buf.readUtf(32);
            String ownerName = buf.readUtf(32);
            int playerCount = buf.readVarInt();
            boolean passwordProtected = buf.readBoolean();
            boolean persistent = buf.readBoolean();
            int userBlockCount = buf.readVarInt();
            int pSize = buf.readVarInt();
            List<String> players = new ArrayList<>(pSize);
            for (int j = 0; j < pSize; j++) {
                players.add(buf.readUtf(32));
            }
            list.add(new ChannelInfo(freq, name, ownerName, playerCount, players, passwordProtected, persistent, userBlockCount));
        }
        this.channels = Collections.unmodifiableList(list);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(channels.size());
        for (ChannelInfo ch : channels) {
            buf.writeUtf(ch.frequency(), 16);
            buf.writeUtf(ch.name(), 32);
            buf.writeUtf(ch.ownerName(), 32);
            buf.writeVarInt(ch.playerCount());
            buf.writeBoolean(ch.passwordProtected());
            buf.writeBoolean(ch.persistent());
            buf.writeVarInt(ch.userBlockCount());
            buf.writeVarInt(ch.players().size());
            for (String p : ch.players()) {
                buf.writeUtf(p, 32);
            }
        }
    }

    public List<ChannelInfo> channels() {
        return channels;
    }

    public static void handle(PacketSyncChannels pkt) {
        com.Theus452.walkietalkie.client.ChannelCache.set(pkt.channels());
        java.util.Set<String> activeFreqs = new java.util.HashSet<>();
        for (ChannelInfo ch : pkt.channels()) {
            activeFreqs.add(ch.frequency());
        }
        com.Theus452.walkietalkie.client.ChannelMessageCache.retainFrequencies(activeFreqs);
    }
}
