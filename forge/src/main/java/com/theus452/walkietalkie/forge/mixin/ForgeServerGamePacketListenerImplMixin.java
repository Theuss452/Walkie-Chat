package com.theus452.walkietalkie.forge.mixin;

import com.theus452.walkietalkie.item.WalkieTalkieItem;
import com.theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ForgeServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleChat(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;)V", at = @At("HEAD"), cancellable = true)
    private void onHandleChat(ServerboundChatPacket packet, CallbackInfo ci) {
        final net.minecraft.server.MinecraftServer server = this.player.getServer();
        if (server == null) {
            return;
        }

        String messageContent = packet.message();
        ItemStack mainHand = this.player.getMainHandItem();

        if (mainHand.getItem() instanceof WalkieTalkieItem) {
            String frequency = WalkieTalkieItem.getFrequency(mainHand);
            if (frequency.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
                ci.cancel();
                return;
            }

            Component walkieTalkieMessage = Component.literal("§a[Walkie-Talkie] §f<" + player.getDisplayName().getString() + ">§f " + messageContent);
            player.sendSystemMessage(walkieTalkieMessage);

            for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
                if (receiver == player) continue;
                for (ItemStack inventoryStack : receiver.getInventory().items) {
                    if (inventoryStack.getItem() instanceof WalkieTalkieItem && frequency.equals(WalkieTalkieItem.getFrequency(inventoryStack))) {
                        receiver.sendSystemMessage(walkieTalkieMessage);
                        break;
                    }
                }
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