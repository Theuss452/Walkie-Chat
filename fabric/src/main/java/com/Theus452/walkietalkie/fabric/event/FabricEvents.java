package com.Theus452.walkietalkie.fabric.event;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.compat.AttractToChatCompat;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.util.ConnectionManager;
import com.Theus452.walkietalkie.util.WalkieMessageHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricEvents {
    private static final Logger SERVER_LOGGER = LoggerFactory.getLogger("net.minecraft.server.MinecraftServer");

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, typeKey) -> {
            if (AttractToChatCompat.isVocallyMuted(sender)) {
                sender.displayClientMessage(Component.translatable("message.walkietalkie.vocal_muted"), true);
                return false;
            }

            ItemStack walkieStack = sender.getMainHandItem();
            if (!(walkieStack.getItem() instanceof WalkieTalkieItem)) {
                walkieStack = sender.getOffhandItem();
            }

            MinecraftServer server = sender.getServer();
            if (server == null) {
                return false;
            }

            SERVER_LOGGER.info("<{}> {}", sender.getDisplayName().getString(), message.signedContent());

            if (walkieStack.getItem() instanceof WalkieTalkieItem) {
                String frequency = WalkieTalkieItem.getFrequency(walkieStack);
                if (frequency.isEmpty()) {
                    sender.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
                } else {
                    WalkieMessageHelper.broadcastMessage(server, sender, frequency, message.signedContent());
                }
            } else {
                WalkieTalkieBlockEntity nearbyWalkie = WalkieMessageHelper.findNearbyActiveBlock(sender);
                if (nearbyWalkie != null) {
                    WalkieMessageHelper.broadcastMessage(server, sender, nearbyWalkie.getFrequency(), message.signedContent());
                    return false;
                }
                Component formattedMessage = Component.translatable("chat.type.text", sender.getDisplayName(), Component.literal(message.signedContent()));
                double currentChatRange = AttractToChatCompat.getEffectiveProximityRange(
                        message.signedContent(), Platform.getHelper().getChatRange());
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

        ServerTickEvents.END_SERVER_TICK.register(ConnectionManager::tick);
    }
}
