package com.Theus452.walkietalkie.util;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerRateLimiter {
    private static final Map<Key, Long> LAST_ACCEPTED_TICK = new ConcurrentHashMap<>();

    private ServerRateLimiter() {
    }

    public static boolean allow(ServerPlayer player, String action, long intervalTicks) {
        if (player == null || action == null || action.isBlank()) {
            return false;
        }
        long now = player.getLevel() == null ? 0L : player.getLevel().getGameTime();
        long safeInterval = Math.max(0L, intervalTicks);
        Key key = new Key(player.getUUID(), action);
        boolean[] accepted = {false};
        LAST_ACCEPTED_TICK.compute(key, (k, previous) -> {
            if (previous != null && now - previous < safeInterval) {
                return previous;
            }
            accepted[0] = true;
            return now;
        });
        if (LAST_ACCEPTED_TICK.size() > 4096) {
            LAST_ACCEPTED_TICK.entrySet().removeIf(entry -> now - entry.getValue() > 20L * 60L);
        }
        return accepted[0];
    }

    public static void clearPlayer(UUID uuid) {
        if (uuid == null) return;
        LAST_ACCEPTED_TICK.keySet().removeIf(key -> uuid.equals(key.playerId));
    }

    public static void clearAll() {
        LAST_ACCEPTED_TICK.clear();
    }

    private record Key(UUID playerId, String action) {
    }
}
