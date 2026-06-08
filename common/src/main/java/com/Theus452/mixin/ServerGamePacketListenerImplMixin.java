package com.Theus452.mixin;

import com.Theus452.walkietalkie.api.WalkieChatCallback;
import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.channel.ChannelManager;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.networking.WalkieNetworkHandler;
import com.Theus452.walkietalkie.util.WalkieMessageHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void onHandleChat(ServerboundChatPacket packet, CallbackInfo ci) {
        String content = packet.message();
        if (content.startsWith("/")) return;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof WalkieTalkieItem)) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
        }

        boolean holdingActive = false;
        String frequency = "";

        if (stack.getItem() instanceof WalkieTalkieItem) {
            frequency = WalkieTalkieItem.getFrequency(stack);
            holdingActive = !frequency.isEmpty();
        }

        if (holdingActive) {
            if (!ChannelManager.canAccess(player, frequency)) {
                player.sendSystemMessage(Component.translatable("gui.walkietalkie.error.private_access"));
                ci.cancel();
                return;
            }
            WalkieMessageHelper.broadcastMessage(player.server, player, frequency, content);
            if (WalkieChatCallback.PROXIMITY_CHAT.hasListeners()) {
                WalkieChatCallback.PROXIMITY_CHAT.invoke(new WalkieChatCallback.ProximityData(player, content, List.of(), 0));
            }
            ci.cancel();
            return;
        }

        WalkieTalkieBlockEntity nearbyWalkie = WalkieMessageHelper.findNearbyActiveBlock(player);
        if (nearbyWalkie != null) {
            WalkieMessageHelper.broadcastMessage(player.server, player, nearbyWalkie.getFrequency(), content);
            if (WalkieChatCallback.PROXIMITY_CHAT.hasListeners()) {
                WalkieChatCallback.PROXIMITY_CHAT.invoke(new WalkieChatCallback.ProximityData(player, content, List.of(), 0));
            }
            ci.cancel();
            return;
        }

        double range = Platform.getHelper().getChatRange();
        List<ServerPlayer> players = player.getServer().getPlayerList().getPlayers();
        Component formattedMessage = Component.translatable("chat.type.text", player.getDisplayName(), content);

        int recipients = 0;
        List<ServerPlayer> proxPlayers = new ArrayList<>();

        for (ServerPlayer other : players) {
            if (other == player) {
                other.sendSystemMessage(formattedMessage);
                continue;
            }
            if (player.distanceToSqr(other) <= range * range) {
                other.sendSystemMessage(formattedMessage);
                proxPlayers.add(other);
                recipients++;
            }
        }

        if (recipients == 0 && players.size() > 1) {
            player.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby"));
        }

        if (WalkieChatCallback.PROXIMITY_CHAT.hasListeners()) {
            WalkieChatCallback.PROXIMITY_CHAT.invoke(new WalkieChatCallback.ProximityData(player, content, proxPlayers, range));
        }

        ci.cancel();
    }

    private void broadcastRadioMessage(String msg, String freq) {
        List<ServerPlayer> players = player.getServer().getPlayerList().getPlayers();
        List<ServerPlayer> receivers = new ArrayList<>();

        Component hoverText = Component.translatable("tooltip.walkietalkie.frequency.chat", freq);
        Component chatMessage = Component.literal("§a[Walkie-Talkie]§7[" + freq + "]")
                .withStyle(style -> style.withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, hoverText)))
                .copy()
                .append(Component.literal(" §f<" + player.getDisplayName().getString() + "> " + msg));

        player.sendSystemMessage(chatMessage);
        WalkieNetworkHandler.sendPushMessage(player, freq, player.getName().getString(), msg);
        player.playNotifySound(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_SEND_MSG.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.0f);

        for (ServerPlayer receiver : players) {
            if (receiver == player) continue;
            if (ChannelManager.hasTunedWalkie(receiver, freq)) {
                receiver.sendSystemMessage(chatMessage);
                WalkieNetworkHandler.sendPushMessage(receiver, freq, player.getName().getString(), msg);
                receivers.add(receiver);
                
                com.Theus452.walkietalkie.util.IncomingMessageSoundLimiter.SoundDecision soundDecision = com.Theus452.walkietalkie.util.IncomingMessageSoundLimiter.evaluate(receiver);
                if (soundDecision.shouldPlay()) {
                    receiver.playNotifySound(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_MSG_RECEIVER.get(), net.minecraft.sounds.SoundSource.PLAYERS, soundDecision.volume(), soundDecision.pitch());
                }
            }
        }

        if (WalkieChatCallback.WALKIE_BROADCAST.hasListeners()) {
            WalkieChatCallback.WALKIE_BROADCAST.invoke(new WalkieChatCallback.BroadcastData(player, msg, freq, receivers));
        }
    }
}
