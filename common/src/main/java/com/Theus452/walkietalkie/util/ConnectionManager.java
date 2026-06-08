package com.Theus452.walkietalkie.util;

import com.Theus452.walkietalkie.channel.ChannelManager;
import com.Theus452.walkietalkie.channel.ChannelRegistry;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.networking.WalkieNetworkHandler;
import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ConnectionManager {
    private static final Map<UUID, Map<String, Long>> DISCONNECTION_TIMERS = new HashMap<>();
    private static final Map<UUID, Set<String>> ACTIVE_FREQUENCIES = new HashMap<>();
    private static final Map<UUID, Integer> LAST_SENT_HASHES = new HashMap<>();
    private static final long TIMEOUT = 30_000L;
    private static int tickCounter;
    private static MinecraftServer activeServer;

    private ConnectionManager() {
    }

    public static void playerDroppedWalkieTalkie(ServerPlayer player, String frequency) {
        if (countWalkieTalkiesWithFrequency(player, frequency) == 0) {
            DISCONNECTION_TIMERS
                    .computeIfAbsent(player.getUUID(), key -> new HashMap<>())
                    .put(frequency, System.currentTimeMillis() + TIMEOUT);
        }
    }

    public static void playerPickedUpWalkieTalkie(ServerPlayer player, String frequency, int expectedCount) {
        UUID playerId = player.getUUID();
        boolean recoveredConnection = false;
        Map<String, Long> timers = DISCONNECTION_TIMERS.get(playerId);
        if (timers != null) {
            recoveredConnection = timers.remove(frequency) != null;
            if (timers.isEmpty()) {
                DISCONNECTION_TIMERS.remove(playerId);
            }
        }
        if (countWalkieTalkiesWithFrequency(player, frequency) != expectedCount) {
            return;
        }
        player.sendSystemMessage(Component.translatable("message.walkietalkie.join.self", frequency).withStyle(ChatFormatting.GREEN));
        if (recoveredConnection) {
            return;
        }
        Component joinMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                .append(Component.translatable("message.walkietalkie.join.other", player.getDisplayName())
                        .withStyle(ChatFormatting.YELLOW));
        for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
            if (otherPlayer != player && ChannelManager.hasTunedWalkie(otherPlayer, frequency)) {
                otherPlayer.sendSystemMessage(joinMessage);
            }
        }
    }

    public static void cancelDisconnect(ServerPlayer player, String frequency) {
        UUID playerId = player.getUUID();
        Map<String, Long> timers = DISCONNECTION_TIMERS.get(playerId);
        if (timers != null) {
            timers.remove(frequency);
            if (timers.isEmpty()) {
                DISCONNECTION_TIMERS.remove(playerId);
            }
        }
        Set<String> frequencies = ACTIVE_FREQUENCIES.get(playerId);
        if (frequencies != null) {
            frequencies.remove(frequency);
        }
    }

    public static void onPlayerPlacedWalkieBlock(ServerPlayer player, String frequency) {
        if (player != null && !frequency.isEmpty() && player.server != null) {
            syncActiveChannels(player.server);
        }
    }

    public static void disconnectImmediatelyIfAbsent(ServerPlayer player, String frequency) {
        if (player == null || frequency == null || frequency.isEmpty() || countWalkieTalkiesWithFrequency(player, frequency) > 0) {
            return;
        }
        cancelDisconnect(player, frequency);
        if (player.server != null) {
            syncActiveChannels(player.server);
        }
    }

    public static void refreshPlayer(ServerPlayer player) {
        if (player != null && player.server != null) {
            syncActiveChannels(player.server);
        }
    }

    public static void tick(MinecraftServer server) {
        ensureServer(server);
        tickCounter++;
        ChannelManager.cleanup(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ChannelManager.sanitizePlayerWalkies(player);
            UUID playerId = player.getUUID();
            Set<String> currentFrequencies = collectCurrentFrequencies(player);
            Set<String> previousFrequencies = ACTIVE_FREQUENCIES.getOrDefault(playerId, Set.of());
            for (String frequency : previousFrequencies) {
                if (!currentFrequencies.contains(frequency) && !hasDisconnectTimer(playerId, frequency)) {
                    playerDroppedWalkieTalkie(player, frequency);
                }
            }
            for (String frequency : currentFrequencies) {
                if (!previousFrequencies.contains(frequency)) {
                    playerPickedUpWalkieTalkie(player, frequency, countWalkieTalkiesWithFrequency(player, frequency));
                }
            }
            ACTIVE_FREQUENCIES.put(playerId, currentFrequencies);
        }
        Set<UUID> onlinePlayers = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            onlinePlayers.add(player.getUUID());
        }
        ACTIVE_FREQUENCIES.keySet().removeIf(playerId -> !onlinePlayers.contains(playerId));
        LAST_SENT_HASHES.keySet().removeIf(playerId -> !onlinePlayers.contains(playerId));

        long now = System.currentTimeMillis();
        boolean expiredAny = expireDisconnectTimers(server, now);
        boolean removedAny = removeEmptyChannels(server, now);
        if (expiredAny || removedAny || tickCounter % 10 == 0) {
            syncActiveChannels(server);
        }
    }

    public static void syncActiveChannels(MinecraftServer server) {
        ensureServer(server);
        Map<String, List<String>> activePlayers = buildActivePlayers(server);
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            sendChannels(server, viewer, activePlayers, false);
        }
    }

    public static void syncActiveChannels(MinecraftServer server, ServerPlayer viewer, boolean force) {
        ensureServer(server);
        sendChannels(server, viewer, buildActivePlayers(server), force);
    }

    public static boolean removeEmptyChannels(MinecraftServer server) {
        ensureServer(server);
        return removeEmptyChannels(server, System.currentTimeMillis());
    }

    private static void removeFrequency(String frequency) {
        for (Map<String, Long> timers : DISCONNECTION_TIMERS.values()) {
            timers.remove(frequency);
        }
        DISCONNECTION_TIMERS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        for (Set<String> frequencies : ACTIVE_FREQUENCIES.values()) {
            frequencies.remove(frequency);
        }
        LAST_SENT_HASHES.clear();
    }

    private static void ensureServer(MinecraftServer server) {
        if (activeServer != server) {
            DISCONNECTION_TIMERS.clear();
            ACTIVE_FREQUENCIES.clear();
            LAST_SENT_HASHES.clear();
            tickCounter = 0;
            activeServer = server;
        }
    }

    private static void sendChannels(
            MinecraftServer server,
            ServerPlayer viewer,
            Map<String, List<String>> activePlayers,
            boolean force
    ) {
        Map<String, ChannelRegistry.ChannelDefinition> definitions = new LinkedHashMap<>();
        for (ChannelRegistry.ChannelDefinition definition : ChannelRegistry.get(server).getChannels()) {
            definitions.put(definition.frequency(), definition);
        }
        Set<String> frequencies = new HashSet<>(definitions.keySet());
        frequencies.addAll(activePlayers.keySet());
        List<PacketSyncChannels.ChannelInfo> channels = new ArrayList<>(frequencies.size());
        for (String frequency : frequencies) {
            ChannelRegistry.ChannelDefinition definition = definitions.get(frequency);
            List<String> players = activePlayers.getOrDefault(frequency, List.of());
            boolean passwordProtected = definition != null && definition.passwordProtected();
            boolean mayInspect = !passwordProtected || ChannelManager.canAccess(viewer, frequency);
            String name = definition == null
                    ? Component.translatable("gui.walkietalkie.unnamed_channel", frequency).getString()
                    : definition.name();
            channels.add(new PacketSyncChannels.ChannelInfo(
                    frequency,
                    name,
                    mayInspect ? players.size() : -1,
                    mayInspect ? players : List.of(),
                    passwordProtected,
                    definition != null
            ));
        }
        channels.sort(Comparator
                .comparing(PacketSyncChannels.ChannelInfo::persistent).reversed()
                .thenComparingInt(info -> Integer.parseInt(info.frequency())));
        int hash = channels.hashCode();
        Integer previousHash = LAST_SENT_HASHES.get(viewer.getUUID());
        if (force || previousHash == null || previousHash != hash) {
            LAST_SENT_HASHES.put(viewer.getUUID(), hash);
            WalkieNetworkHandler.sendSyncChannels(viewer, channels);
        }
    }

    private static Map<String, List<String>> buildActivePlayers(MinecraftServer server) {
        Map<String, List<String>> activePlayers = new HashMap<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Set<String> frequencies = collectCurrentFrequencies(player);
            Map<String, Long> timers = DISCONNECTION_TIMERS.get(player.getUUID());
            if (timers != null) {
                long now = System.currentTimeMillis();
                for (Map.Entry<String, Long> entry : timers.entrySet()) {
                    if (now <= entry.getValue() && ChannelManager.canAccess(player, entry.getKey())) {
                        frequencies.add(entry.getKey());
                    }
                }
            }
            for (String frequency : frequencies) {
                activePlayers
                        .computeIfAbsent(frequency, key -> new ArrayList<>())
                        .add(player.getDisplayName().getString());
            }
        }
        return activePlayers;
    }

    private static Set<String> collectCurrentFrequencies(ServerPlayer player) {
        Set<String> frequencies = new HashSet<>();
        WalkieBlockRegistry.addOwnedFrequencies(player.getUUID(), frequencies);
        collectCurrentFrequencies(player, player.getInventory().items, frequencies);
        collectCurrentFrequencies(player, player.getInventory().offhand, frequencies);
        return frequencies;
    }

    private static void collectCurrentFrequencies(ServerPlayer player, List<ItemStack> stacks, Set<String> frequencies) {
        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                String frequency = WalkieTalkieItem.getFrequency(stack);
                if (!frequency.isEmpty() && ChannelManager.canAccess(player, frequency)) {
                    frequencies.add(frequency);
                }
            }
        }
    }

    private static boolean expireDisconnectTimers(MinecraftServer server, long now) {
        boolean[] expiredAny = {false};
        DISCONNECTION_TIMERS.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            entry.getValue().entrySet().removeIf(timerEntry -> {
                if (now <= timerEntry.getValue()) {
                    return false;
                }
                expiredAny[0] = true;
                String frequency = timerEntry.getKey();
                boolean disconnected = player == null || countWalkieTalkiesWithFrequency(player, frequency) == 0;
                if (disconnected) {
                    Component lostMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                            .append(Component.translatable(
                                    "message.walkietalkie.lost_connection",
                                    player != null ? player.getDisplayName() : "A player"
                            ).withStyle(ChatFormatting.YELLOW));
                    for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                        if (!otherPlayer.getUUID().equals(playerId)
                                && ChannelManager.hasTunedWalkie(otherPlayer, frequency)) {
                            otherPlayer.sendSystemMessage(lostMessage);
                        }
                    }
                    if (player != null) {
                        ChannelManager.revokeAccess(player, frequency);
                    }
                }
                return true;
            });
            return entry.getValue().isEmpty();
        });
        return expiredAny[0];
    }

    private static boolean removeEmptyChannels(MinecraftServer server, long now) {
        Set<String> occupiedFrequencies = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            occupiedFrequencies.addAll(collectCurrentFrequencies(player));
        }
        for (Map<String, Long> timers : DISCONNECTION_TIMERS.values()) {
            for (Map.Entry<String, Long> timer : timers.entrySet()) {
                if (now <= timer.getValue()) {
                    occupiedFrequencies.add(timer.getKey());
                }
            }
        }
        boolean removedAny = false;
        ChannelRegistry registry = ChannelRegistry.get(server);
        for (ChannelRegistry.ChannelDefinition definition : registry.getChannels()) {
            if (!occupiedFrequencies.contains(definition.frequency())) {
                registry.remove(definition.frequency());
                ChannelManager.revokeFrequencyAccess(server, definition.frequency());
                removeFrequency(definition.frequency());
                removedAny = true;
            }
        }
        return removedAny;
    }

    private static boolean hasDisconnectTimer(UUID playerId, String frequency) {
        Map<String, Long> timers = DISCONNECTION_TIMERS.get(playerId);
        return timers != null && timers.containsKey(frequency);
    }

    private static int countWalkieTalkiesWithFrequency(ServerPlayer player, String frequency) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem
                    && frequency.equals(WalkieTalkieItem.getFrequency(stack))
                    && ChannelManager.canAccess(player, frequency)) {
                count++;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof WalkieTalkieItem
                    && frequency.equals(WalkieTalkieItem.getFrequency(stack))
                    && ChannelManager.canAccess(player, frequency)) {
                count++;
            }
        }
        if (WalkieBlockRegistry.hasOwnedFrequency(player.getUUID(), frequency)) {
            count++;
        }
        return count;
    }
}
