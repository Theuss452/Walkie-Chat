package com.Theus452.walkietalkie.channel;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.networking.WalkieNetworkHandler;
import com.Theus452.walkietalkie.networking.packet.ChannelActionType;
import com.Theus452.walkietalkie.sound.ModSounds;
import com.Theus452.walkietalkie.util.ConnectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ChannelManager {
    private static final Map<UUID, Set<String>> PRIVATE_ACCESS = new HashMap<>();
    private static MinecraftServer activeServer;

    private ChannelManager() {
    }

    public static void createChannel(
            ServerPlayer player,
            InteractionHand hand,
            String rawFrequency,
            String rawName,
            boolean passwordProtected,
            String password,
            long requestId
    ) {
        String frequency = normalizeFrequency(rawFrequency);
        String name = normalizeName(rawName);
        if (!isValidFrequency(frequency)) {
            sendResult(player, false, "", "gui.walkietalkie.error.frequency", ChannelActionType.CREATE, requestId);
            return;
        }
        if (!isValidName(name)) {
            sendResult(player, false, "", "gui.walkietalkie.error.name", ChannelActionType.CREATE, requestId);
            return;
        }
        if (passwordProtected && !isValidPassword(password)) {
            sendResult(player, false, "", "gui.walkietalkie.error.password_length", ChannelActionType.CREATE, requestId);
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WalkieTalkieItem)) {
            sendResult(player, false, "", "gui.walkietalkie.error.item", ChannelActionType.CREATE, requestId);
            return;
        }
        ChannelRegistry registry = ChannelRegistry.get(player.server);
        if (registry.getChannel(frequency) != null) {
            sendResult(player, false, "", "gui.walkietalkie.error.frequency_used", ChannelActionType.CREATE, requestId);
            return;
        }
        ChannelRegistry.ChannelDefinition definition = registry.create(
                frequency,
                name,
                player.getUUID(),
                player.getDisplayName().getString(),
                passwordProtected,
                password
        );
        if (definition == null) {
            sendResult(player, false, "", "gui.walkietalkie.error.frequency_used", ChannelActionType.CREATE, requestId);
            return;
        }
        if (passwordProtected) {
            grantAccess(player, frequency);
        }
        tune(player, hand, frequency);
        sendResult(player, true, frequency, "gui.walkietalkie.success.created", ChannelActionType.CREATE, requestId);
        ConnectionManager.syncActiveChannels(player.server);
    }

    public static void joinChannel(
            ServerPlayer player,
            InteractionHand hand,
            String rawFrequency,
            String password,
            long requestId
    ) {
        String frequency = normalizeFrequency(rawFrequency);
        if (!isValidFrequency(frequency)) {
            sendResult(player, false, "", "gui.walkietalkie.error.frequency", ChannelActionType.JOIN, requestId);
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WalkieTalkieItem)) {
            sendResult(player, false, "", "gui.walkietalkie.error.item", ChannelActionType.JOIN, requestId);
            return;
        }
        ChannelRegistry.ChannelDefinition definition = ChannelRegistry.get(player.server).getChannel(frequency);
        if (definition == null) {
            sendResult(player, false, "", "gui.walkietalkie.error.channel_not_found", ChannelActionType.JOIN, requestId);
            return;
        }
        if (definition.passwordProtected()) {
            if (!definition.matchesPassword(password)) {
                sendResult(player, false, "", "gui.walkietalkie.error.password", ChannelActionType.JOIN, requestId);
                return;
            }
            grantAccess(player, frequency);
        }
        tune(player, hand, frequency);
        sendResult(player, true, frequency, "gui.walkietalkie.success.joined", ChannelActionType.JOIN, requestId);
        ConnectionManager.syncActiveChannels(player.server);
    }

    public static void setFrequency(
            ServerPlayer player,
            InteractionHand hand,
            String rawFrequency,
            long requestId
    ) {
        String frequency = normalizeFrequency(rawFrequency);
        if (!frequency.isEmpty()) {
            sendResult(player, false, "", "gui.walkietalkie.error.channel_not_found", ChannelActionType.JOIN, requestId);
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WalkieTalkieItem)) {
            sendResult(player, false, "", "gui.walkietalkie.error.item", ChannelActionType.LEAVE, requestId);
            return;
        }
        String oldFrequency = WalkieTalkieItem.getFrequency(stack);
        if (!oldFrequency.isEmpty()) {
            tune(player, hand, "");
        }
        sendResult(player, true, "", "gui.walkietalkie.success.left", ChannelActionType.LEAVE, requestId);
        ConnectionManager.syncActiveChannels(player.server);
    }

    public static boolean canAccess(ServerPlayer player, String frequency) {
        if (frequency == null || frequency.isEmpty()) {
            return false;
        }
        ensureServer(player.server);
        ChannelRegistry.ChannelDefinition definition = ChannelRegistry.get(player.server).getChannel(frequency);
        if (definition == null) {
            return false;
        }
        return !definition.passwordProtected() || hasAccess(player, frequency);
    }

    public static boolean hasTunedWalkie(ServerPlayer player, String frequency) {
        if (!canAccess(player, frequency)) {
            return false;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                return true;
            }
        }
        return false;
    }

    public static void revokeAccess(ServerPlayer player, String frequency) {
        ensureServer(player.server);
        Set<String> frequencies = PRIVATE_ACCESS.get(player.getUUID());
        if (frequencies == null) {
            return;
        }
        frequencies.remove(frequency);
        if (frequencies.isEmpty()) {
            PRIVATE_ACCESS.remove(player.getUUID());
        }
    }

    public static void revokeFrequencyAccess(MinecraftServer server, String frequency) {
        ensureServer(server);
        for (Set<String> frequencies : PRIVATE_ACCESS.values()) {
            frequencies.remove(frequency);
        }
        PRIVATE_ACCESS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public static void sanitizePlayerWalkies(ServerPlayer player) {
        boolean changed = false;
        changed |= sanitizeStacks(player, player.getInventory().items);
        changed |= sanitizeStacks(player, player.getInventory().offhand);
        if (changed) {
            player.getInventory().setChanged();
            player.sendSystemMessage(
                    Component.translatable("message.walkietalkie.channel_access_cleared")
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    public static void cleanup(MinecraftServer server) {
        ensureServer(server);
        Iterator<UUID> iterator = PRIVATE_ACCESS.keySet().iterator();
        while (iterator.hasNext()) {
            UUID playerId = iterator.next();
            if (server.getPlayerList().getPlayer(playerId) == null) {
                iterator.remove();
            }
        }
    }

    public static boolean isValidFrequency(String frequency) {
        if (frequency == null || frequency.isEmpty() || frequency.length() > 3) {
            return false;
        }
        for (int i = 0; i < frequency.length(); i++) {
            if (!Character.isDigit(frequency.charAt(i))) {
                return false;
            }
        }
        try {
            int value = Integer.parseInt(frequency);
            return value >= 1 && value <= 999;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public static boolean isValidName(String name) {
        if (name == null || name.length() < 1 || name.length() > 16) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            if (!net.minecraft.SharedConstants.isAllowedChatCharacter(character)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPassword(String password) {
        return password != null && password.length() >= 4 && password.length() <= 32;
    }

    private static String normalizeFrequency(String frequency) {
        if (frequency == null) {
            return "";
        }
        String normalized = frequency.trim();
        int index = 0;
        while (index < normalized.length() - 1 && normalized.charAt(index) == '0') {
            index++;
        }
        return normalized.substring(index);
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", " ");
    }

    private static void grantAccess(ServerPlayer player, String frequency) {
        ensureServer(player.server);
        PRIVATE_ACCESS.computeIfAbsent(player.getUUID(), key -> new HashSet<>()).add(frequency);
    }

    private static boolean hasAccess(ServerPlayer player, String frequency) {
        ensureServer(player.server);
        return PRIVATE_ACCESS.getOrDefault(player.getUUID(), Set.of()).contains(frequency);
    }

    private static void ensureServer(MinecraftServer server) {
        if (activeServer != server) {
            PRIVATE_ACCESS.clear();
            activeServer = server;
        }
    }

    private static void leaveChannel(ServerPlayer player, String frequency) {
        clearFrequencyFromPlayer(player, frequency);
        revokeAccess(player, frequency);
        ConnectionManager.cancelDisconnect(player, frequency);
        Component leaveMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                .append(Component.translatable("message.walkietalkie.leave.other", player.getDisplayName())
                        .withStyle(ChatFormatting.YELLOW));
        for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
            if (otherPlayer != player && hasTunedWalkie(otherPlayer, frequency)) {
                otherPlayer.sendSystemMessage(leaveMessage);
            }
        }
        player.playNotifySound(ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        ConnectionManager.removeEmptyChannels(player.server);
    }

    private static void tune(ServerPlayer player, InteractionHand hand, String frequency) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WalkieTalkieItem)) {
            return;
        }
        String oldFrequency = WalkieTalkieItem.getFrequency(stack);
        if (oldFrequency.equals(frequency)) {
            return;
        }
        boolean keepsOldFrequency = hasOtherWalkieWithFrequency(player, oldFrequency, stack);
        if (!oldFrequency.isEmpty() && !keepsOldFrequency) {
            ConnectionManager.cancelDisconnect(player, oldFrequency);
            revokeAccess(player, oldFrequency);
            Component leaveMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                    .append(Component.translatable("message.walkietalkie.leave.other", player.getDisplayName())
                            .withStyle(ChatFormatting.YELLOW));
            for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
                if (otherPlayer != player && hasTunedWalkie(otherPlayer, oldFrequency)) {
                    otherPlayer.sendSystemMessage(leaveMessage);
                }
            }
        }
        WalkieTalkieItem.setFrequency(stack, frequency);
        player.getInventory().setChanged();
        player.playNotifySound(ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!oldFrequency.isEmpty() && !keepsOldFrequency) {
            ConnectionManager.removeEmptyChannels(player.server);
        }
    }

    private static boolean clearFrequencyFromPlayer(ServerPlayer player, String frequency) {
        boolean changed = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                WalkieTalkieItem.setFrequency(stack, "");
                changed = true;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                WalkieTalkieItem.setFrequency(stack, "");
                changed = true;
            }
        }
        if (changed) {
            player.getInventory().setChanged();
        }
        return changed;
    }

    private static boolean sanitizeStacks(ServerPlayer player, Iterable<ItemStack> stacks) {
        boolean changed = false;
        ChannelRegistry registry = ChannelRegistry.get(player.server);
        for (ItemStack stack : stacks) {
            if (!(stack.getItem() instanceof WalkieTalkieItem)) {
                continue;
            }
            String frequency = WalkieTalkieItem.getFrequency(stack);
            if (frequency.isEmpty()) {
                continue;
            }
            ChannelRegistry.ChannelDefinition definition = registry.getChannel(frequency);
            if (definition == null || definition.passwordProtected() && !hasAccess(player, frequency)) {
                WalkieTalkieItem.setFrequency(stack, "");
                changed = true;
            }
        }
        return changed;
    }

    private static boolean hasOtherWalkieWithFrequency(ServerPlayer player, String frequency, ItemStack excludedStack) {
        if (frequency.isEmpty()) {
            return false;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack != excludedStack
                    && stack.getItem() instanceof WalkieTalkieItem
                    && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack != excludedStack
                    && stack.getItem() instanceof WalkieTalkieItem
                    && frequency.equals(WalkieTalkieItem.getFrequency(stack))) {
                return true;
            }
        }
        return false;
    }

    private static void sendResult(
            ServerPlayer player,
            boolean success,
            String frequency,
            String messageKey,
            ChannelActionType actionType,
            long requestId
    ) {
        WalkieNetworkHandler.sendChannelActionResult(
                player,
                success,
                frequency,
                messageKey,
                actionType,
                requestId
        );
    }
}
