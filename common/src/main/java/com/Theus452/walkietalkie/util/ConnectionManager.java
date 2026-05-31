package com.Theus452.walkietalkie.util;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
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
import java.util.ArrayList;
import java.util.List;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;

public class ConnectionManager {
    private static final Map<UUID, Map<String, Long>> disconnectionTimers = new HashMap<>();
    private static final Map<UUID, Set<String>> activeFrequencies = new HashMap<>();
    private static final long TIMEOUT = 30 * 1000;
    private static List<PacketSyncChannels.ChannelInfo> lastSentChannels = null;
    private static int tickCounter = 0;

    public static void playerDroppedWalkieTalkie(ServerPlayer player, String frequency) {
        if (countConnectionsWithFrequency(player, frequency) == 0) {
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

        if (countConnectionsWithFrequency(player, frequency) == expectedCount) {
            player.sendSystemMessage(Component.translatable("message.walkietalkie.join.self", frequency).withStyle(ChatFormatting.GREEN));

            if (!recoveredConnection) {
                Component joinMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                        .append(Component.translatable("message.walkietalkie.join.other", player.getDisplayName())
                                .withStyle(ChatFormatting.YELLOW));

                for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
                    if (otherPlayer == player) continue;
                    if (hasConnectionWithFrequency(otherPlayer, frequency)) {
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

    public static void onPlayerPlacedWalkieBlock(ServerPlayer player, String frequency) {
        if (player == null || frequency == null || frequency.isEmpty()) return;
        refreshPlayer(player);
    }

    public static void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter % 10 == 0) {
            syncActiveChannels(server);
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            refreshPlayer(player);
        }

        activeFrequencies.keySet().removeIf(uuid -> server.getPlayerList().getPlayer(uuid) == null);

        long currentTime = System.currentTimeMillis();
        disconnectionTimers.entrySet().removeIf(entry -> {
            UUID playerUUID = entry.getKey();
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);

            entry.getValue().entrySet().removeIf(freqEntry -> {
                String frequency = freqEntry.getKey();
                if (currentTime > freqEntry.getValue()) {
                    if (player == null || countConnectionsWithFrequency(player, frequency) == 0) {
                        Component lostConnectionMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                                .append(Component.translatable("message.walkietalkie.lost_connection", player != null ? player.getDisplayName() : "A player")
                                        .withStyle(ChatFormatting.YELLOW));

                        for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                            if (otherPlayer.getUUID().equals(playerUUID)) continue;
                            if (hasConnectionWithFrequency(otherPlayer, frequency)) {
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

    public static void refreshPlayer(ServerPlayer player) {
        if (player == null) return;
        UUID playerUUID = player.getUUID();
        Set<String> currentFrequencies = collectConnectionFrequencies(player);
        Set<String> lastFrequencies = activeFrequencies.getOrDefault(playerUUID, Set.of());

        for (String frequency : lastFrequencies) {
            if (!currentFrequencies.contains(frequency)) {
                boolean hasTimer = disconnectionTimers.containsKey(playerUUID) && disconnectionTimers.get(playerUUID).containsKey(frequency);
                if (!hasTimer) {
                    playerDroppedWalkieTalkie(player, frequency);
                }
            }
        }

        for (String frequency : currentFrequencies) {
            if (!lastFrequencies.contains(frequency)) {
                playerPickedUpWalkieTalkie(player, frequency, countConnectionsWithFrequency(player, frequency));
            }
        }

        activeFrequencies.put(playerUUID, currentFrequencies);
    }

    public static void disconnectImmediatelyIfAbsent(ServerPlayer player, String frequency) {
        if (player == null || frequency == null || frequency.isEmpty() || hasConnectionWithFrequency(player, frequency)) return;
        cancelDisconnect(player, frequency);
        notifyFrequencyLeft(player, frequency);
    }

    public static boolean hasConnectionWithFrequency(ServerPlayer player, String frequency) {
        return countConnectionsWithFrequency(player, frequency) > 0;
    }

    private static Set<String> collectConnectionFrequencies(ServerPlayer player) {
        Set<String> frequencies = new HashSet<>();
        WalkieBlockRegistry.addOwnedFrequencies(player.getUUID(), frequencies);
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                String frequency = WalkieTalkieItem.getFrequency(stack);
                if (!frequency.isEmpty()) frequencies.add(frequency);
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                String frequency = WalkieTalkieItem.getFrequency(stack);
                if (!frequency.isEmpty()) frequencies.add(frequency);
            }
        }
        return frequencies;
    }

    private static int countConnectionsWithFrequency(ServerPlayer player, String frequency) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                count++;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                count++;
            }
        }
        if (WalkieBlockRegistry.hasOwnedFrequency(player.getUUID(), frequency)) {
            count++;
        }
        return count;
    }

    private static void notifyFrequencyLeft(ServerPlayer player, String frequency) {
        Component leaveMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
            .append(Component.translatable("message.walkietalkie.leave.other", player.getDisplayName()).withStyle(ChatFormatting.YELLOW));
        for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
            if (otherPlayer != player && hasConnectionWithFrequency(otherPlayer, frequency)) {
                otherPlayer.sendSystemMessage(countTotalWalkieTalkies(otherPlayer) > 1
                    ? leaveMessage.copy().append(Component.literal(" [" + frequency + "]").withStyle(ChatFormatting.GRAY))
                    : leaveMessage);
            }
        }
    }

    private static int countTotalWalkieTalkies(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                count++;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                count++;
            }
        }
        Set<String> ownedFrequencies = new HashSet<>();
        WalkieBlockRegistry.addOwnedFrequencies(player.getUUID(), ownedFrequencies);
        count += ownedFrequencies.size();
        return count;
    }
    public static void syncActiveChannels(MinecraftServer server) {
        Map<String, Integer> freqCounts = new HashMap<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (String freq : collectConnectionFrequencies(player)) {
                freqCounts.put(freq, freqCounts.getOrDefault(freq, 0) + 1);
            }
        }
        List<PacketSyncChannels.ChannelInfo> currentChannels = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : freqCounts.entrySet()) {
            currentChannels.add(new PacketSyncChannels.ChannelInfo(entry.getKey(), entry.getValue()));
        }
        currentChannels.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a.frequency()), Integer.parseInt(b.frequency()));
            } catch (NumberFormatException e) {
                return a.frequency().compareTo(b.frequency());
            }
        });
        boolean changed = lastSentChannels == null || lastSentChannels.size() != currentChannels.size();
        if (!changed) {
            for (int i = 0; i < currentChannels.size(); i++) {
                PacketSyncChannels.ChannelInfo a = lastSentChannels.get(i);
                PacketSyncChannels.ChannelInfo b = currentChannels.get(i);
                if (!a.frequency().equals(b.frequency()) || a.playerCount() != b.playerCount()) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            lastSentChannels = currentChannels;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                com.Theus452.walkietalkie.networking.WalkieNetworkHandler.sendSyncChannels(player, currentChannels);
            }
        }
    }
}
