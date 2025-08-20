package com.theus452.mixin;

import com.theus452.walkietalkie.item.WalkieTalkieItem;
import com.theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique; 
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Unique 
    private int walkietalkie$countWalkieTalkies(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof WalkieTalkieItem) {
                count++;
            }
        }
        return count;
    }

    @Unique 
    private MutableComponent walkietalkie$createWalkieTalkieMessage(ServerPlayer sender, String rawText, String frequency, boolean showFrequency) {
        MutableComponent prefix;
        if (showFrequency) {
            prefix = Component.literal("§a[Walkie-Talkie]§7[" + frequency + "]");
        } else {
            prefix = Component.literal("§a[Walkie-Talkie]");
        }
        Component messageBody = Component.literal(" §f<" + sender.getDisplayName().getString() + "> " + rawText);
        return prefix.append(messageBody);
    }

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void onHandleChat(ServerboundChatPacket packet, CallbackInfo ci) {
        String messageContent = packet.message();
        ItemStack mainHand = this.player.getMainHandItem();
        final net.minecraft.server.MinecraftServer server = this.player.getServer();
        if (server == null) {
            return;
        }

        if (mainHand.getItem() instanceof WalkieTalkieItem) {
            String frequency = WalkieTalkieItem.getFrequency(mainHand);
            if (frequency.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
                ci.cancel();
                return;
            }

            
            int senderWalkieTalkieCount = walkietalkie$countWalkieTalkies(player);
            boolean shouldShowFrequencyToSender = senderWalkieTalkieCount > 1;

            Component senderMessage = walkietalkie$createWalkieTalkieMessage(player, messageContent, frequency, shouldShowFrequencyToSender);
            player.sendSystemMessage(senderMessage);

            for (ServerPlayer receiver : player.getServer().getPlayerList().getPlayers()) {
                if (receiver == player) continue;
                for (ItemStack inventoryStack : receiver.getInventory().items) {
                    if (inventoryStack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                        int receiverWalkieTalkieCount = walkietalkie$countWalkieTalkies(receiver);
                        boolean shouldShowFrequencyToReceiver = receiverWalkieTalkieCount > 1;
                        Component receiverMessage = walkietalkie$createWalkieTalkieMessage(player, messageContent, frequency, shouldShowFrequencyToReceiver);
                        receiver.sendSystemMessage(receiverMessage);
                        break;
                    }
                }
            }
            ci.cancel();

        } else {
            Component formattedMessage = Component.translatable("chat.type.text", player.getDisplayName(), Component.literal(messageContent));
            double currentChatRange = Platform.HELPER.getChatRange();
            int recipientsFound = 0;
            for (ServerPlayer recipient : player.getServer().getPlayerList().getPlayers()) {
                if (player.distanceToSqr(recipient) <= currentChatRange * currentChatRange) {
                    recipient.sendSystemMessage(formattedMessage);
                    recipientsFound++;
                }
            }

            if (recipientsFound <= 1 && player.getServer().getPlayerList().getPlayerCount() > 1) {
                player.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
            ci.cancel();
        }
    }
}