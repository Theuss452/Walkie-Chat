package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

        
        if (!oldFrequency.isEmpty()) {
            Component leaveMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                    .append(Component.translatable("message.walkietalkie.leave.other", player.getDisplayName())
                            .withStyle(ChatFormatting.YELLOW));

            for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
                if (otherPlayer == player) continue;
                for (ItemStack inventoryStack : otherPlayer.getInventory().items) {
                    if (inventoryStack.getItem() instanceof WalkieTalkieItem) {
                        if (oldFrequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                            otherPlayer.sendSystemMessage(leaveMessage);
                            break;
                        }
                    }
                }
            }
        }

        WalkieTalkieItem.setFrequency(stack, packet.newFrequency);

        
        if (!packet.newFrequency.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.walkietalkie.join.self", packet.newFrequency).withStyle(ChatFormatting.GREEN));

            Component joinMessage = Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN)
                    .append(Component.translatable("message.walkietalkie.join.other", player.getDisplayName())
                            .withStyle(ChatFormatting.YELLOW));

            for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
                if (otherPlayer == player) continue;
                for (ItemStack inventoryStack : otherPlayer.getInventory().items) {
                    if (inventoryStack.getItem() instanceof WalkieTalkieItem) {
                        if (packet.newFrequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                            otherPlayer.sendSystemMessage(joinMessage);
                            break;
                        }
                    }
                }
            }
        }
    }
}