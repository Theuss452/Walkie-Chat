package com.Theus452.walkietalkie.client;

import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ChannelCache {
    private static volatile List<PacketSyncChannels.ChannelInfo> channels = Collections.emptyList();
    private static volatile long lastUpdateMs = 0;

    public static List<PacketSyncChannels.ChannelInfo> get() {
        return channels;
    }

    public static void set(List<PacketSyncChannels.ChannelInfo> newChannels) {
        channels = newChannels != null ? Collections.unmodifiableList(new ArrayList<>(newChannels)) : Collections.emptyList();
        lastUpdateMs = System.currentTimeMillis();
    }

    public static long millisSinceUpdate() {
        return lastUpdateMs == 0 ? Long.MAX_VALUE : System.currentTimeMillis() - lastUpdateMs;
    }

    public static void clear() {
        channels = Collections.emptyList();
        lastUpdateMs = 0;
    }
}
