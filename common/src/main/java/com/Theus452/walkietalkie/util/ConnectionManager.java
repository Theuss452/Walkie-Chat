package com.Theus452.walkietalkie.util;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ConnectionManager {
    private static final Map<UUID, Map<String, Long>> disconnectionTimers = new HashMap<>();
    private static final Map<UUID, Set<String>> activeFrequencies = new HashMap<>();
    private static final long TIMEOUT = 30 * 1000;

    public static void playerDroppedWalkieTalkie(ServerPlayer player, String frequency) {
        if (countWalkieTalkiesWithFrequency(player, frequency) == 0) {
            UUID playerUUID = player.getUUID();
            disconnectionTimers.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(frequency, System.currentTimeMillis() + TIMEOUT);
        }
    }

    public static void playerPickedUpWalkieTalkie(ServerPlayer player, String frequency, int expectedCount) {
        UUID playerUUID = player.getUUID();
        boolean recoveredConnection = false;

        if (disconnectionTimers.containsKey(playerUUID)) {
            if (disconnectionTimers.get(playerUUID).remove(frequency) != null) {
                recoveredConnection = true;
            }
            if (disconnectionTimers.get(playerUUID).isEmpty()) {
                disconnectionTimers.remove(playerUUID);
            }
        }

        if (countWalkieTalkiesWithFrequency(player, frequency) == expectedCount) {
            player.sendSystemMessage(Component.translatable("message.walkietalkie.join.self", frequency).withStyle(ChatFormatting.GREEN));

            if (!recoveredConnection) {
                Component joinMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                        .append(Component.translatable("message.walkietalkie.join.other", player.getDisplayName())
                                .withStyle(ChatFormatting.YELLOW));

                for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
                    if (otherPlayer == player) continue;
                    if (hasWalkieTalkieWithFrequency(otherPlayer, frequency)) {
                        if (countTotalWalkieTalkies(otherPlayer) > 1) {
                            otherPlayer.sendSystemMessage(joinMessage.copy().append(Component.literal(" [" + frequency + "]").withStyle(ChatFormatting.GRAY)));
                        } else {
                            otherPlayer.sendSystemMessage(joinMessage);
                        }
                    }
                }
            }
        }
    }

    public static void cancelDisconnect(ServerPlayer player, String frequency) {
        UUID playerUUID = player.getUUID();

        if (disconnectionTimers.containsKey(playerUUID)) {
            disconnectionTimers.get(playerUUID).remove(frequency);
            if (disconnectionTimers.get(playerUUID).isEmpty()) {
                disconnectionTimers.remove(playerUUID);
            }
        }

        if (activeFrequencies.containsKey(playerUUID)) {
            activeFrequencies.get(playerUUID).remove(frequency);
        }
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerUUID = player.getUUID();
            Set<String> currentFrequencies = new HashSet<>();

            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof WalkieTalkieItem) {
                    String freq = WalkieTalkieItem.getFrequency(stack);
                    if (!freq.isEmpty()) {
                        currentFrequencies.add(freq);
                    }
                }
            }

            Set<String> lastFrequencies = activeFrequencies.getOrDefault(playerUUID, new HashSet<>());

            for (String freq : lastFrequencies) {
                if (!currentFrequencies.contains(freq)) {
                    boolean hasTimer = disconnectionTimers.containsKey(playerUUID) &&
                            disconnectionTimers.get(playerUUID).containsKey(freq);

                    if (!hasTimer) {
                        playerDroppedWalkieTalkie(player, freq);
                    }
                }
            }

            for (String freq : currentFrequencies) {
                if (!lastFrequencies.contains(freq)) {
                    playerPickedUpWalkieTalkie(player, freq, countWalkieTalkiesWithFrequency(player, freq));
                }
            }

            activeFrequencies.put(playerUUID, currentFrequencies);
        }

        activeFrequencies.keySet().removeIf(uuid -> server.getPlayerList().getPlayer(uuid) == null);

        long currentTime = System.currentTimeMillis();
        disconnectionTimers.entrySet().removeIf(entry -> {
            UUID playerUUID = entry.getKey();
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);

            entry.getValue().entrySet().removeIf(freqEntry -> {
                String frequency = freqEntry.getKey();
                if (currentTime > freqEntry.getValue()) {
                    if (player == null || countWalkieTalkiesWithFrequency(player, frequency) == 0) {
                        Component lostConnectionMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                                .append(Component.translatable("message.walkietalkie.lost_connection", player != null ? player.getDisplayName() : "A player")
                                        .withStyle(ChatFormatting.YELLOW));

                        for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                            if (otherPlayer.getUUID().equals(playerUUID)) continue;
                            if (hasWalkieTalkieWithFrequency(otherPlayer, frequency)) {
                                if (countTotalWalkieTalkies(otherPlayer) > 1) {
                                    otherPlayer.sendSystemMessage(lostConnectionMessage.copy().append(Component.literal(" [" + frequency + "]").withStyle(ChatFormatting.GRAY)));
                                } else {
                                    otherPlayer.sendSystemMessage(lostConnectionMessage);
                                }
                            }
                        }
                    }
                    return true;
                }
                return false;
            });
            return entry.getValue().isEmpty();
        });
    }

    private static int countWalkieTalkiesWithFrequency(ServerPlayer player, String frequency) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                count++;
            }
        }
        return count;
    }

    private static boolean hasWalkieTalkieWithFrequency(ServerPlayer player, String frequency) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                return true;
            }
        }
        return false;
    }

    private static int countTotalWalkieTalkies(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                count++;
            }
        }
        return count;
    }
}