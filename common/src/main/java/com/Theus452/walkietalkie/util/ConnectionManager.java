package com.Theus452.walkietalkie.util;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

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


    public static void ignoreFrequencyChange(ServerPlayer player, String frequency) {
        UUID playerUUID = player.getUUID();

        if (activeFrequencies.containsKey(playerUUID)) {
            activeFrequencies.get(playerUUID).remove(frequency);
        }

        if (disconnectionTimers.containsKey(playerUUID)) {
            disconnectionTimers.get(playerUUID).remove(frequency);
        }
    }

    public static void playerPickedUpWalkieTalkie(ServerPlayer player, String frequency, int expectedCount) {
        UUID playerUUID = player.getUUID();
        boolean isReconnecting = false;

        if (disconnectionTimers.containsKey(playerUUID) && disconnectionTimers.get(playerUUID).containsKey(frequency)) {
            disconnectionTimers.get(playerUUID).remove(frequency);
            isReconnecting = true;
            if (disconnectionTimers.get(playerUUID).isEmpty()) {
                disconnectionTimers.remove(playerUUID);
            }
        }

        if (countWalkieTalkiesWithFrequency(player, frequency) == expectedCount) {
            player.sendSystemMessage(Component.translatable("message.walkietalkie.join.self", frequency).withStyle(ChatFormatting.GREEN));

            if (!isReconnecting) {
                Component joinMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                        .append(Component.translatable("message.walkietalkie.join.other", player.getDisplayName())
                                .withStyle(ChatFormatting.YELLOW));

                for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
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

    public static void tick(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Set<String> currentFrequencies = getPlayerFrequencies(player);
            UUID playerId = player.getUUID();

            Set<String> cached = activeFrequencies.getOrDefault(playerId, new HashSet<>());

            for (String oldFreq : cached) {
                if (!currentFrequencies.contains(oldFreq)) {
                    playerDroppedWalkieTalkie(player, oldFreq);
                }
            }

            activeFrequencies.put(playerId, currentFrequencies);
        }

        activeFrequencies.keySet().removeIf(uuid -> server.getPlayerList().getPlayer(uuid) == null);

        disconnectionTimers.entrySet().removeIf(entry -> {
            UUID playerUUID = entry.getKey();
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);

            entry.getValue().entrySet().removeIf(freqEntry -> {
                String frequency = freqEntry.getKey();
                if (currentTime > freqEntry.getValue()) {
                    if (player == null || countWalkieTalkiesWithFrequency(player, frequency) == 0) {
                        Component lostConnectionMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                                .append(Component.translatable("message.walkietalkie.lost_connection",
                                                player != null ? player.getDisplayName() : Component.literal("A player"))
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

    private static Set<String> getPlayerFrequencies(ServerPlayer player) {
        Set<String> freqs = new HashSet<>();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                String f = WalkieTalkieItem.getFrequency(stack);
                if (!f.isEmpty()) {
                    freqs.add(f);
                }
            }
        }
        return freqs;
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