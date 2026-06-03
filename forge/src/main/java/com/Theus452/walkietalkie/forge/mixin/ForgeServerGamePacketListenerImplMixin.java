package com.Theus452.walkietalkie.forge.mixin;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ForgeServerGamePacketListenerImplMixin {

    private static final Logger SERVER_LOGGER = LoggerFactory.getLogger("net.minecraft.server.MinecraftServer");

    @Shadow public ServerPlayer player;

    @Unique
    private int walkietalkie$countWalkieTalkies(ServerPlayer player) {
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

    @Inject(method = "handleChat(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;)V", at = @At("HEAD"), cancellable = true)
    private void onHandleChat(ServerboundChatPacket packet, CallbackInfo ci) {
        String messageContent = packet.message();

        SERVER_LOGGER.info("<{}> {}", player.getDisplayName().getString(), messageContent);

        final net.minecraft.server.MinecraftServer server = this.player.getServer();
        if (server == null) {
            return;
        }

        ItemStack walkieStack = this.player.getMainHandItem();
        if (!(walkieStack.getItem() instanceof WalkieTalkieItem)) {
            walkieStack = this.player.getOffhandItem();
        }

        if (walkieStack.getItem() instanceof WalkieTalkieItem) {
            String frequency = WalkieTalkieItem.getFrequency(walkieStack);
            if (frequency.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
                ci.cancel();
                return;
            }

            int senderWalkieTalkieCount = walkietalkie$countWalkieTalkies(player);
            boolean shouldShowFrequencyToSender = senderWalkieTalkieCount > 1;

            Component senderMessage = walkietalkie$createWalkieTalkieMessage(player, messageContent, frequency, shouldShowFrequencyToSender);
            player.sendSystemMessage(senderMessage);
            player.playNotifySound(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_SEND_MSG.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

            int recipientsFound = 0;

            for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
                if (receiver == player) continue;
                boolean hasReceiverWalkie = false;


                for (ItemStack inventoryStack : receiver.getInventory().items) {
                    if (inventoryStack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                        hasReceiverWalkie = true;
                        break;
                    }
                }

                if (!hasReceiverWalkie) {
                    for (ItemStack inventoryStack : receiver.getInventory().offhand) {
                        if (inventoryStack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                            hasReceiverWalkie = true;
                            break;
                        }
                    }
                }

                if (hasReceiverWalkie) {
                    int receiverWalkieTalkieCount = walkietalkie$countWalkieTalkies(receiver);
                    boolean shouldShowFrequencyToReceiver = receiverWalkieTalkieCount > 1;
                    Component receiverMessage = walkietalkie$createWalkieTalkieMessage(player, messageContent, frequency, shouldShowFrequencyToReceiver);
                    receiver.sendSystemMessage(receiverMessage);
                    com.Theus452.walkietalkie.util.IncomingMessageSoundLimiter.SoundDecision soundDecision = com.Theus452.walkietalkie.util.IncomingMessageSoundLimiter.evaluate(receiver);
                    if (soundDecision.shouldPlay()) {
                        receiver.playNotifySound(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_MSG_RECEIVER.get(), net.minecraft.sounds.SoundSource.PLAYERS, soundDecision.volume(), soundDecision.pitch());
                    }
                    recipientsFound++;
                }
            }

            if (recipientsFound == 0) {
                player.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_on_frequency")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }

            ci.cancel();

        } else {
            Component formattedMessage = Component.translatable("chat.type.text", player.getDisplayName(), Component.literal(messageContent));
            double currentChatRange = Platform.HELPER.getChatRange();
            int recipientsFound = 0;
            for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
                if (player.distanceToSqr(recipient) <= currentChatRange * currentChatRange) {
                    recipient.sendSystemMessage(formattedMessage);
                    recipientsFound++;
                }
            }

            if (recipientsFound <= 1 && server.getPlayerList().getPlayerCount() > 1) {
                player.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
            ci.cancel();
        }
    }
}