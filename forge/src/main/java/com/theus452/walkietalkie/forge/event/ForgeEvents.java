package com.theus452.walkietalkie.forge.event;

import com.mojang.brigadier.CommandDispatcher;
import com.theus452.walkietalkie.forge.commands.ForgeCommands;
import com.theus452.walkietalkie.item.WalkieTalkieItem;
import com.theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEvents {

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ForgeCommands.register(dispatcher);
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        ItemStack mainHand = sender.getMainHandItem();
        event.setCanceled(true);

        MinecraftServer server = sender.getServer();
        if (server == null) {
            return;
        }

        if (mainHand.getItem() instanceof WalkieTalkieItem) {
            String frequency = WalkieTalkieItem.getFrequency(mainHand);
            if (frequency.isEmpty()) {
                sender.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
                return;
            }

            int senderWalkieTalkieCount = countWalkieTalkies(sender);
            sender.sendSystemMessage(createWalkieTalkieMessage(sender, event.getRawText(), frequency, senderWalkieTalkieCount > 1));

            for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
                if (receiver == sender) continue;
                for (ItemStack inventoryStack : receiver.getInventory().items) {
                    if (inventoryStack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                        int walkieTalkieCount = countWalkieTalkies(receiver);
                        Component messageToSend = createWalkieTalkieMessage(sender, event.getRawText(), frequency, walkieTalkieCount > 1);
                        receiver.sendSystemMessage(messageToSend);
                        break;
                    }
                }
            }
        } else {
            Component formattedMessage = Component.translatable("chat.type.text", sender.getDisplayName(), Component.literal(event.getRawText()));
            double currentChatRange = Platform.HELPER.getChatRange();
            int recipientsFound = 0;

            for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
                if (sender.distanceToSqr(recipient) <= currentChatRange * currentChatRange) {
                    recipient.sendSystemMessage(formattedMessage);
                    recipientsFound++;
                }
            }

            if (recipientsFound <= 1 && server.getPlayerList().getPlayerCount() > 1) {
                sender.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
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

    private static Component createWalkieTalkieMessage(ServerPlayer sender, String rawText, String frequency, boolean showFrequencyInText) {
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