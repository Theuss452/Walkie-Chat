package com.Theus452.walkietalkie.util;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class WalkieBlockMessageRelay {
    private static final int MESSAGE_LIFETIME_TICKS = 4;
    private static final double LISTEN_RANGE_SQ = 64.0D;
    private static final Map<MinecraftServer, ServerRelayState> SERVER_STATES = new WeakHashMap<>();

    private WalkieBlockMessageRelay() {
    }

    public static void publish(MinecraftServer server, ServerPlayer sender, String frequency, String rawText, Collection<ServerPlayer> deliveredPlayers) {
        if (server == null || sender == null || frequency == null || frequency.isEmpty() || rawText == null || rawText.isEmpty()) return;
        ServerRelayState state = SERVER_STATES.computeIfAbsent(server, ignored -> new ServerRelayState());
        removeExpired(state, server.getTickCount());
        Set<UUID> delivered = new HashSet<>();
        if (deliveredPlayers != null) {
            for (ServerPlayer player : deliveredPlayers) {
                if (player != null) delivered.add(player.getUUID());
            }
        }
        state.messages.addLast(new RelayMessage(++state.sequence, server.getTickCount() + MESSAGE_LIFETIME_TICKS, sender.getUUID(), frequency, rawText, delivered));
    }

    public static long deliver(WalkieTalkieBlockEntity block, ServerLevel level, long lastProcessedSequence) {
        if (block == null || level == null || !block.isActive() || !block.isRelayEnabled()) return lastProcessedSequence;
        MinecraftServer server = level.getServer();
        ServerRelayState state = SERVER_STATES.get(server);
        if (state == null) return lastProcessedSequence;
        removeExpired(state, server.getTickCount());
        long newestSequence = lastProcessedSequence;

        for (RelayMessage message : state.messages) {
            if (message.sequence <= lastProcessedSequence) continue;
            newestSequence = Math.max(newestSequence, message.sequence);
            if (!block.getFrequency().equals(message.frequency)) continue;
            ServerPlayer sender = server.getPlayerList().getPlayer(message.senderId);
            if (sender == null) continue;
            AABB listenerBounds = new AABB(block.getBlockPos()).inflate(8.0D);
            Collection<ServerPlayer> soundListeners = new ArrayList<>();
            double x = block.getBlockPos().getX() + 0.5D;
            double y = block.getBlockPos().getY() + 0.5D;
            double z = block.getBlockPos().getZ() + 0.5D;

            for (ServerPlayer listener : level.getEntitiesOfClass(ServerPlayer.class, listenerBounds)) {
                if (listener.getUUID().equals(message.senderId)) continue;
                if (listener.distanceToSqr(x, y, z) > LISTEN_RANGE_SQ) continue;
                if (!message.deliveredPlayers.add(listener.getUUID())) continue;
                listener.sendSystemMessage(WalkieMessageHelper.createWalkieTalkieMessage(sender, message.rawText, message.frequency, WalkieMessageHelper.countWalkieTalkies(listener) > 1));
                soundListeners.add(listener);
            }

            if (!soundListeners.isEmpty()) {
                block.markMessageReceived();
                block.playReceiveSound(soundListeners);
            }
        }

        return newestSequence;
    }

    private static void removeExpired(ServerRelayState state, int currentTick) {
        while (!state.messages.isEmpty() && state.messages.peekFirst().expiresAtTick < currentTick) {
            state.messages.removeFirst();
        }
    }

    private static final class ServerRelayState {
        private final ArrayDeque<RelayMessage> messages = new ArrayDeque<>();
        private long sequence;
    }

    private static final class RelayMessage {
        private final long sequence;
        private final int expiresAtTick;
        private final UUID senderId;
        private final String frequency;
        private final String rawText;
        private final Set<UUID> deliveredPlayers;

        private RelayMessage(long sequence, int expiresAtTick, UUID senderId, String frequency, String rawText, Set<UUID> deliveredPlayers) {
            this.sequence = sequence;
            this.expiresAtTick = expiresAtTick;
            this.senderId = senderId;
            this.frequency = frequency;
            this.rawText = rawText;
            this.deliveredPlayers = deliveredPlayers;
        }
    }
}
