package com.Theus452.walkietalkie.forge.event;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEvents {

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        ItemStack mainHand = sender.getMainHandItem();

        
        event.setCanceled(true);

        if (mainHand.getItem() instanceof WalkieTalkieItem) {
            
            String frequency = WalkieTalkieItem.getFrequency(mainHand);
            if (frequency.isEmpty()) {
                sender.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
                return;
            }

            
            Component walkieTalkieMessage = Component.literal("§a[Walkie-Talkie] §f<" + sender.getDisplayName().getString() + ">§f " + event.getRawText());

            
            sender.sendSystemMessage(walkieTalkieMessage);

            
            for (ServerPlayer receiver : sender.getServer().getPlayerList().getPlayers()) {
                if (receiver == sender) continue;
                for (ItemStack inventoryStack : receiver.getInventory().items) {
                    if (inventoryStack.getItem() instanceof WalkieTalkieItem) {
                        if (frequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                            receiver.sendSystemMessage(walkieTalkieMessage);
                            break;
                        }
                    }
                }
            }
        } else {
            
            Component formattedMessage = Component.translatable("chat.type.text", sender.getDisplayName(), Component.literal(event.getRawText()));
            double currentChatRange = Platform.HELPER.getChatRange();
            int recipientsFound = 0;

            for (ServerPlayer recipient : sender.getServer().getPlayerList().getPlayers()) {
                if (sender.distanceToSqr(recipient) <= currentChatRange * currentChatRange) {
                    recipient.sendSystemMessage(formattedMessage);
                    recipientsFound++;
                }
            }

            if (recipientsFound <= 1 && sender.getServer().getPlayerList().getPlayerCount() > 1) {
                sender.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    }
}