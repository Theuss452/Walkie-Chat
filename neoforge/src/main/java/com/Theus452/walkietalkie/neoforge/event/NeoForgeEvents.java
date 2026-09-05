package com.Theus452.walkietalkie.neoforge.event;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.compat.AttractToChatCompat;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.neoforge.commands.NeoForgeCommands;
import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.util.ConnectionManager;
import com.Theus452.walkietalkie.util.WalkieMessageHelper;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeoForgeEvents {

    private static final Logger SERVER_LOGGER = LoggerFactory.getLogger("net.minecraft.server.MinecraftServer");

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        NeoForgeCommands.register(dispatcher);
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        if (AttractToChatCompat.isVocallyMuted(sender)) {
            sender.displayClientMessage(Component.translatable("message.walkietalkie.vocal_muted"), true);
            event.setCanceled(true);
            return;
        }

        ItemStack walkieStack = sender.getMainHandItem();
        if (!(walkieStack.getItem() instanceof WalkieTalkieItem)) {
            walkieStack = sender.getOffhandItem();
        }

        event.setCanceled(true);

        MinecraftServer server = sender.getServer();
        if (server == null) {
            return;
        }

        SERVER_LOGGER.info("<{}> {}", sender.getDisplayName().getString(), event.getRawText());

        if (walkieStack.getItem() instanceof WalkieTalkieItem) {
            String frequency = WalkieTalkieItem.getFrequency(walkieStack);
            if (frequency.isEmpty()) {
                sender.sendSystemMessage(Component.translatable("message.walkietalkie.define.frequency"));
                return;
            }

            WalkieMessageHelper.broadcastMessage(server, sender, frequency, event.getRawText());
        } else {
            WalkieTalkieBlockEntity nearbyWalkie = WalkieMessageHelper.findNearbyActiveBlock(sender);
            if (nearbyWalkie != null) {
                WalkieMessageHelper.broadcastMessage(server, sender, nearbyWalkie.getFrequency(), event.getRawText());
                return;
            }

            Component formattedMessage = Component.translatable("chat.type.text", sender.getDisplayName(), Component.literal(event.getRawText()));
            double currentChatRange = AttractToChatCompat.getEffectiveProximityRange(
                    event.getRawText(), Platform.getHelper().getChatRange());
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

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (event.getEntity().getOwner() instanceof ServerPlayer player && event.getEntity().getItem().getItem() instanceof WalkieTalkieItem) {
            String frequency = WalkieTalkieItem.getFrequency(event.getEntity().getItem());
            if (!frequency.isEmpty()) {
                ConnectionManager.playerDroppedWalkieTalkie(player, frequency);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        ConnectionManager.tick(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        WalkieBlockRegistry.clearOnStop();
    }
}
