package com.Theus452.walkietalkie.util;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
import com.Theus452.walkietalkie.sound.ModSounds;
import net.minecraft.ChatFormatting;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WalkieMessageHelper {
    public static final double BLOCK_LISTEN_RANGE_SQ = 64.0D;

    private WalkieMessageHelper() {
    }

    public static WalkieTalkieBlockEntity findNearbyActiveBlock(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return null;
        }
        List<GlobalPos> activeBlocks = WalkieBlockRegistry.getBlocksByRange(level, player.blockPosition(), 8.0D);
        for (GlobalPos globalPos : activeBlocks) {
            if (!globalPos.dimension().equals(level.dimension())) continue;
            BlockEntity be = level.getBlockEntity(globalPos.pos());
            if (be instanceof WalkieTalkieBlockEntity walkie && walkie.isActive() && !walkie.getFrequency().isEmpty()) {
                if (player.distanceToSqr(globalPos.pos().getX() + 0.5D, globalPos.pos().getY() + 0.5D, globalPos.pos().getZ() + 0.5D) <= BLOCK_LISTEN_RANGE_SQ) {
                    return walkie;
                }
            }
        }
        return null;
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
        List<GlobalPos> activeBlocks = WalkieBlockRegistry.getBlocks(frequency, server);

        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (receiver == sender) continue;
            if (hasWalkieTalkieWithFrequency(receiver, frequency)) {
                recipients.add(receiver);
            }
        }

        for (GlobalPos globalPos : activeBlocks) {
            ServerLevel level = server.getLevel(globalPos.dimension());
            if (level == null) continue;
            BlockEntity be = level.getBlockEntity(globalPos.pos());
            if (be instanceof WalkieTalkieBlockEntity walkie && walkie.isActive()) {
                walkie.markMessageReceived();
                if (ModSounds.WALKIE_TALKIE_MSG_RECEIVER != null) {
                    level.playSound(null, globalPos.pos(), ModSounds.WALKIE_TALKIE_MSG_RECEIVER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                double bx = globalPos.pos().getX() + 0.5D;
                double by = globalPos.pos().getY() + 0.5D;
                double bz = globalPos.pos().getZ() + 0.5D;
                for (ServerPlayer nearbyPlayer : level.players()) {
                    if (nearbyPlayer == sender) continue;
                    if (nearbyPlayer.distanceToSqr(bx, by, bz) <= BLOCK_LISTEN_RANGE_SQ) {
                        recipients.add(nearbyPlayer);
                    }
                }
            }
        }

        for (ServerPlayer recipient : recipients) {
            Component messageToSend = createWalkieTalkieMessage(sender, rawText, frequency, countWalkieTalkies(recipient) > 1);
            recipient.sendSystemMessage(messageToSend);
            IncomingMessageSoundLimiter.SoundDecision soundDecision = IncomingMessageSoundLimiter.evaluate(recipient);
            if (soundDecision.shouldPlay() && ModSounds.WALKIE_TALKIE_MSG_RECEIVER != null) {
                recipient.playNotifySound(ModSounds.WALKIE_TALKIE_MSG_RECEIVER.get(), SoundSource.PLAYERS, soundDecision.volume(), soundDecision.pitch());
            }
        }

        if (recipients.isEmpty()) {
            sender.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_on_frequency").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        return true;
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
