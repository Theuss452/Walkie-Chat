package com.Theus452.mixin;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.compat.AttractToChatCompat;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.util.WalkieMessageHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void walkietalkie$onChat(ServerboundChatPacket packet, CallbackInfo ci) {
        String content = packet.message();
        if (content == null) {
            return;
        }
        content = content.strip();
        if (content.isEmpty()) {
            ci.cancel();
            return;
        }
        if (content.startsWith("/")) {
            return;
        }

        if (AttractToChatCompat.isVocallyMuted(player)) {
            player.displayClientMessage(Component.translatable("message.walkietalkie.vocal_muted"), true);
            ci.cancel();
            return;
        }

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof WalkieTalkieItem)) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        if (stack.getItem() instanceof WalkieTalkieItem) {
            String frequency = WalkieTalkieItem.getFrequency(stack);
            if (frequency.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
            } else {
                WalkieMessageHelper.broadcastMessage(server, player, frequency, content);
            }
            ci.cancel();
            return;
        }

        WalkieTalkieBlockEntity nearbyWalkie = WalkieMessageHelper.findNearbyActiveBlock(player);
        if (nearbyWalkie != null) {
            WalkieMessageHelper.broadcastMessage(server, player, nearbyWalkie.getFrequency(), content);
            ci.cancel();
            return;
        }

        Component formattedMessage = Component.translatable("chat.type.text", player.getDisplayName(), Component.literal(content));
        double currentChatRange = AttractToChatCompat.getEffectiveProximityRange(
                content, Platform.getHelper().getChatRange());
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
