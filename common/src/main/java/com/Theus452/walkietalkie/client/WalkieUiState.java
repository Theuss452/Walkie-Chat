package com.Theus452.walkietalkie.client;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.signal.SignalStrength;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class WalkieUiState {
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static volatile Snapshot lastSnapshot = Snapshot.empty();

    private WalkieUiState() {
    }

    public static Snapshot update(Minecraft minecraft, ItemStack visibleStack,
            boolean holdingWalkie, boolean active, String frequency, String channelName) {
        long now = System.currentTimeMillis();
        String safeFrequency = frequency == null ? "" : frequency;
        SignalStrength signal = safeFrequency.isBlank() ? SignalStrength.NO_SIGNAL : SignalStrength.EXCELLENT;
        Snapshot snapshot = new Snapshot(
            now,
            clock24(now),
            minecraft != null && minecraft.level != null ? minecraft.level.getGameTime() : 0L,
            holdingWalkie,
            visibleStack != null && visibleStack.getItem() instanceof WalkieTalkieItem,
            active,
            safeFrequency,
            channelName == null ? "" : channelName,
            signal,
            "",
            0,
            List.of()
        );
        lastSnapshot = snapshot;
        return snapshot;
    }

    public static Snapshot snapshot() {
        return lastSnapshot;
    }

    public static String clock24() {
        return clock24(System.currentTimeMillis());
    }

    private static String clock24(long epochMillis) {
        return LocalTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()).format(CLOCK_FORMAT);
    }

    public record Snapshot(long wallClockMs, String clock24, long clientGameTime,
            boolean holdingWalkie, boolean hasWalkie, boolean active, String frequency,
            String channelName, SignalStrength signal, String environmentReason,
            int onlineCount, List<String> typingPlayers) {
        private static Snapshot empty() {
            long now = System.currentTimeMillis();
            return new Snapshot(now, WalkieUiState.clock24(now), 0L,
                false, false, false, "", "", SignalStrength.NO_SIGNAL, "",
                0, List.of());
        }
    }
}
