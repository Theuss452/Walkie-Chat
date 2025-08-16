package com.theus452.walkietalkie.fabric.event;

import com.theus452.walkietalkie.item.WalkieTalkieItem;
import com.theus452.walkietalkie.platform.Platform;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FabricEvents {
    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, typeKey) -> {
            ItemStack mainHand = sender.getMainHandItem();

            MinecraftServer server = sender.getServer();
            if (server == null) {
                return false;
            }

            if (mainHand.getItem() instanceof WalkieTalkieItem) {
                String frequency = WalkieTalkieItem.getFrequency(mainHand);
                if (frequency.isEmpty()) {
                    sender.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
                } else {
                    int senderWalkieTalkieCount = countWalkieTalkies(sender);
                    sender.sendSystemMessage(createWalkieTalkieMessage(sender, message.signedContent(), frequency, senderWalkieTalkieCount > 1));
                    for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
                        if (receiver == sender) continue;
                        for (ItemStack inventoryStack : receiver.getInventory().items) {
                            if (inventoryStack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                                int walkieTalkieCount = countWalkieTalkies(receiver);
                                Component messageToSend = createWalkieTalkieMessage(sender, message.signedContent(), frequency, walkieTalkieCount > 1);
                                receiver.sendSystemMessage(messageToSend);
                                break;
                            }
                        }
                    }
                }
            } else {
                Component formattedMessage = Component.translatable("chat.type.text", sender.getDisplayName(), Component.literal(message.signedContent()));
                double currentChatRange = Platform.HELPER.getChatRange();
                int recipientsFound = 0;

                for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
                    if (sender.distanceToSqr(recipient) <= currentChatRange * currentChatRange) {
                        recipient.sendSystemMessage(formattedMessage, false);
                        recipientsFound++;
                    }
                }

                if (recipientsFound <= 1 && server.getPlayerList().getPlayerCount() > 1) {
                    sender.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
            }

            return false;
        });
    }

    private static int countWalkieTalkies(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                count++;
            }
        }
        return count;
    }

    private static Component createWalkieTalkieMessage(ServerPlayer sender, String signedContent, String frequency, boolean showFrequencyInText) {
        Component hoverText = Component.translatable("tooltip.walkietalkie.frequency.chat", frequency);
        MutableComponent prefix;

        if (showFrequencyInText) {
            prefix = Component.literal("§a[Walkie-Talkie]§7[" + frequency + "]")
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
        } else {
            prefix = Component.literal("§a[Walkie-Talkie]")
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
        }

        Component messageBody = Component.literal(" §f<" + sender.getDisplayName().getString() + "> " + signedContent);
        return prefix.append(messageBody);
    }
}