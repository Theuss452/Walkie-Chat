package com.Theus452.walkietalkie.util;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
import com.Theus452.walkietalkie.sound.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WalkieMessageHelper {
    public static final double BLOCK_LISTEN_RANGE = 8.0D;
    private static final double BLOCK_LISTEN_RANGE_SQ = BLOCK_LISTEN_RANGE * BLOCK_LISTEN_RANGE;

    private WalkieMessageHelper() {
    }

    public static WalkieTalkieBlockEntity findNearbyActiveBlock(ServerPlayer player) {
        return findNearbyActiveBlock(player, "");
    }

    private static WalkieTalkieBlockEntity findNearbyActiveBlock(ServerPlayer player, String expectedFrequency) {
        if (player == null) {
            return null;
        }
        ServerLevel level = player.getLevel();
        if (level == null) {
            return null;
        }
        List<GlobalPos> activeBlocks = WalkieBlockRegistry.getBlocksByRange(level, player.blockPosition(), BLOCK_LISTEN_RANGE);
        for (GlobalPos globalPos : activeBlocks) {
            if (!globalPos.dimension().equals(level.dimension())) continue;
            BlockEntity be = level.getBlockEntity(globalPos.pos());
            if (isNearbyMatchingBlock(player, be, expectedFrequency)) {
                return (WalkieTalkieBlockEntity) be;
            }
        }
        return findNearbyLoadedBlock(player, level, expectedFrequency);
    }

    public static boolean broadcastMessage(MinecraftServer server, ServerPlayer sender, String frequency, String rawText) {
        if (server == null || sender == null || frequency == null || frequency.isEmpty() || rawText == null || rawText.isEmpty()) {
            return false;
        }

        sender.sendSystemMessage(createWalkieTalkieMessage(sender, rawText, frequency, countWalkieTalkies(sender) > 1));
        if (ModSounds.WALKIE_TALKIE_SEND_MSG != null) {
            sender.playNotifySound(ModSounds.WALKIE_TALKIE_SEND_MSG.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        Set<ServerPlayer> recipients = new HashSet<>();
        Map<WalkieTalkieBlockEntity, Set<ServerPlayer>> blockListeners = new LinkedHashMap<>();
        Set<ServerPlayer> blockRecipients = new HashSet<>();
        List<GlobalPos> activeBlocks = WalkieBlockRegistry.getBlocks(frequency, server);

        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (receiver == sender) continue;
            if (hasWalkieTalkieWithFrequency(receiver, frequency)) {
                recipients.add(receiver);
            }
            WalkieTalkieBlockEntity nearbyBlock = findNearbyActiveBlock(receiver, frequency);
            if (nearbyBlock != null) {
                recipients.add(receiver);
                blockRecipients.add(receiver);
                blockListeners.computeIfAbsent(nearbyBlock, ignored -> new HashSet<>()).add(receiver);
            }
        }

        for (GlobalPos globalPos : activeBlocks) {
            ServerLevel level = server.getLevel(globalPos.dimension());
            if (level == null) continue;
            BlockEntity be = level.getBlockEntity(globalPos.pos());
            if (be instanceof WalkieTalkieBlockEntity walkie && walkie.isActive()) {
                double x = globalPos.pos().getX() + 0.5D;
                double y = globalPos.pos().getY() + 0.5D;
                double z = globalPos.pos().getZ() + 0.5D;
                for (ServerPlayer nearbyPlayer : level.players()) {
                    if (nearbyPlayer != sender && nearbyPlayer.distanceToSqr(x, y, z) <= BLOCK_LISTEN_RANGE_SQ) {
                        recipients.add(nearbyPlayer);
                        blockRecipients.add(nearbyPlayer);
                        blockListeners.computeIfAbsent(walkie, ignored -> new HashSet<>()).add(nearbyPlayer);
                    }
                }
            }
        }

        for (Map.Entry<WalkieTalkieBlockEntity, Set<ServerPlayer>> entry : blockListeners.entrySet()) {
            entry.getKey().markMessageReceived();
            entry.getKey().playReceiveSound(entry.getValue());
        }

        WalkieBlockMessageRelay.publish(server, sender, frequency, rawText, recipients);

        for (ServerPlayer recipient : recipients) {
            Component messageToSend = createWalkieTalkieMessage(sender, rawText, frequency, countWalkieTalkies(recipient) > 1);
            recipient.sendSystemMessage(messageToSend);
            IncomingMessageSoundLimiter.SoundDecision soundDecision = IncomingMessageSoundLimiter.evaluate(recipient);
            if (!blockRecipients.contains(recipient) && soundDecision.shouldPlay() && ModSounds.WALKIE_TALKIE_MSG_RECEIVER != null) {
                recipient.playNotifySound(ModSounds.WALKIE_TALKIE_MSG_RECEIVER.get(), SoundSource.PLAYERS, soundDecision.volume(), soundDecision.pitch());
            }
        }

        if (recipients.isEmpty() && WalkieBlockRegistry.getBlockCount(frequency) == 0) {
            sender.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_on_frequency").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        return true;
    }

    private static boolean isNearbyMatchingBlock(ServerPlayer player, BlockEntity blockEntity, String expectedFrequency) {
        if (!(blockEntity instanceof WalkieTalkieBlockEntity walkie) || !walkie.isActive()) return false;
        if (!expectedFrequency.isEmpty() && !expectedFrequency.equals(walkie.getFrequency())) return false;
        return player.distanceToSqr(walkie.getBlockPos().getX() + 0.5D, walkie.getBlockPos().getY() + 0.5D, walkie.getBlockPos().getZ() + 0.5D) <= BLOCK_LISTEN_RANGE_SQ;
    }

    private static WalkieTalkieBlockEntity findNearbyLoadedBlock(ServerPlayer player, ServerLevel level, String expectedFrequency) {
        int radius = (int) BLOCK_LISTEN_RANGE;
        int minChunkX = (player.getBlockX() - radius) >> 4;
        int maxChunkX = (player.getBlockX() + radius) >> 4;
        int minChunkZ = (player.getBlockZ() - radius) >> 4;
        int maxChunkZ = (player.getBlockZ() + radius) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (isNearbyMatchingBlock(player, blockEntity, expectedFrequency)) {
                        return (WalkieTalkieBlockEntity) blockEntity;
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasWalkieTalkieWithFrequency(ServerPlayer player, String frequency) {
        if (player == null || frequency == null || frequency.isEmpty()) return false;
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

    public static int countWalkieTalkies(ServerPlayer player) {
        if (player == null) return 0;
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
        return count;
    }

    public static Component createWalkieTalkieMessage(ServerPlayer sender, String rawText, String frequency, boolean showFrequencyInText) {
        Component hoverText = Component.translatable("tooltip.walkietalkie.frequency.chat", frequency);
        MutableComponent prefix;

        if (showFrequencyInText) {
            prefix = Component.literal("§a[Walkie-Talkie]§7[" + frequency + "]")
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
        } else {
            prefix = Component.literal("§a[Walkie-Talkie]")
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
        }

        Component messageBody = Component.literal(" §f<" + sender.getDisplayName().getString() + "> " + rawText);
        return prefix.append(messageBody);
    }
}
