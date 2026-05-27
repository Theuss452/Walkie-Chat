package com.Theus452.walkietalkie.util;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class IncomingMessageSoundLimiter {

    private static final long BASE_COOLDOWN_MS = 900L;
    private static final long BACKOFF_STEP_MS = 180L;
    private static final int MAX_BACKOFF_STEPS = 6;
    private static final long BURST_WINDOW_MS = 6_000L;
    private static final long CLEANUP_INTERVAL_MS = 30_000L;
    private static final long STALE_STATE_MS = 5 * 60_000L;

    private static final Map<UUID, PlayerSoundState> PLAYER_STATE = new HashMap<>();
    private static long lastCleanupAtMs;

    private IncomingMessageSoundLimiter() {
    }

    public static SoundDecision evaluate(ServerPlayer receiver) {
        long now = System.currentTimeMillis();
        cleanupStaleState(now);

        PlayerSoundState state = PLAYER_STATE.computeIfAbsent(receiver.getUUID(), ignored -> new PlayerSoundState());
        state.lastActivityAtMs = now;

        while (!state.playedAtMs.isEmpty() && now - state.playedAtMs.peekFirst() > BURST_WINDOW_MS) {
            state.playedAtMs.removeFirst();
        }

        if (now < state.nextAllowedAtMs) {
            return SoundDecision.silent();
        }

        int burstLevel = Math.min(state.playedAtMs.size(), MAX_BACKOFF_STEPS);
        long cooldownMs = BASE_COOLDOWN_MS + burstLevel * BACKOFF_STEP_MS;
        state.nextAllowedAtMs = now + cooldownMs;
        state.playedAtMs.addLast(now);

        float volume = burstLevel >= 3 ? 0.35F : 0.5F;
        float pitch = 0.95F + ThreadLocalRandom.current().nextFloat() * 0.10F;
        return SoundDecision.play(volume, pitch);
    }

    private static void cleanupStaleState(long now) {
        if (now - lastCleanupAtMs < CLEANUP_INTERVAL_MS) {
            return;
        }
        lastCleanupAtMs = now;
        PLAYER_STATE.entrySet().removeIf(entry -> now - entry.getValue().lastActivityAtMs > STALE_STATE_MS);
    }

    private static final class PlayerSoundState {
        private final ArrayDeque<Long> playedAtMs = new ArrayDeque<>();
        private long nextAllowedAtMs;
        private long lastActivityAtMs;
    }

    public static final class SoundDecision {
        private static final SoundDecision SILENT = new SoundDecision(false, 0.0F, 1.0F);

        private final boolean shouldPlay;
        private final float volume;
        private final float pitch;

        private SoundDecision(boolean shouldPlay, float volume, float pitch) {
            this.shouldPlay = shouldPlay;
            this.volume = volume;
            this.pitch = pitch;
        }

        public static SoundDecision silent() {
            return SILENT;
        }

        public static SoundDecision play(float volume, float pitch) {
            return new SoundDecision(true, volume, pitch);
        }

        public boolean shouldPlay() {
            return shouldPlay;
        }

        public float volume() {
            return volume;
        }

        public float pitch() {
            return pitch;
        }
    }
}
