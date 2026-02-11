package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.sound.ModSounds;
import com.Theus452.walkietalkie.util.ConnectionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class PacketSetFrequency {
    private final String newFrequency;
    private final InteractionHand hand;

    public PacketSetFrequency(String frequency, InteractionHand hand) {
        this.newFrequency = frequency;
        this.hand = hand;
    }

    public PacketSetFrequency(FriendlyByteBuf buf) {
        this.newFrequency = buf.readUtf();
        this.hand = buf.readEnum(InteractionHand.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.newFrequency);
        buf.writeEnum(this.hand);
    }

    public static void handle(PacketSetFrequency packet, ServerPlayer player) {
        ItemStack stack = player.getItemInHand(packet.hand);

        if (!(stack.getItem() instanceof WalkieTalkieItem)) {
            return;
        }

        String oldFrequency = WalkieTalkieItem.getFrequency(stack);

        if (oldFrequency.equals(packet.newFrequency)) {
            return;
        }

        boolean hadOldFrequency = hasWalkieTalkieWithFrequency(player, oldFrequency, stack);

        if (!oldFrequency.isEmpty() && !hadOldFrequency) {
            ConnectionManager.cancelDisconnect(player, oldFrequency);

            Component leaveMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                    .append(Component.translatable("message.walkietalkie.leave.other", player.getDisplayName())
                            .withStyle(ChatFormatting.YELLOW));

            for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
                if (otherPlayer == player) continue;
                if (hasWalkieTalkieWithFrequency(otherPlayer, oldFrequency, null)) {
                    int walkieCount = 0;
                    for (ItemStack invStack : otherPlayer.getInventory().items) {
                        if (invStack.getItem() instanceof WalkieTalkieItem) {
                            walkieCount++;
                        }
                    }

                    if (walkieCount > 1) {
                        otherPlayer.sendSystemMessage(leaveMessage.copy().append(Component.literal(" [" + oldFrequency + "]").withStyle(ChatFormatting.GRAY)));
                    } else {
                        otherPlayer.sendSystemMessage(leaveMessage);
                    }
                }
            }
        }

        WalkieTalkieItem.setFrequency(stack, packet.newFrequency);
        player.playNotifySound(ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static boolean hasWalkieTalkieWithFrequency(ServerPlayer player, String frequency, ItemStack excludedStack) {
        for (ItemStack inventoryStack : player.getInventory().items) {
            if (inventoryStack != excludedStack && inventoryStack.getItem() instanceof WalkieTalkieItem) {
                if (frequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                    return true;
                }
            }
        }
        return false;
    }
}
