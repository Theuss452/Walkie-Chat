package com.Theus452.walkietalkie.forge.event;

import com.Theus452.walkietalkie.forge.commands.ForgeCommands;
import com.Theus452.walkietalkie.channel.ChannelManager;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.util.ConnectionManager;
import com.Theus452.walkietalkie.util.WalkieMessageHelper;
import com.Theus452.walkietalkie.compat.AttractToChatCompat;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForgeEvents {

    private static final Logger SERVER_LOGGER = LoggerFactory.getLogger("net.minecraft.server.MinecraftServer");

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ForgeCommands.register(dispatcher);
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        if (AttractToChatCompat.isVocallyMuted(sender)) {
            event.setCanceled(true);
            sender.displayClientMessage(Component.translatable("message.walkietalkie.vocal_muted"), true);
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
            if (!ChannelManager.canAccess(sender, frequency)) {
                sender.sendSystemMessage(Component.translatable("gui.walkietalkie.error.private_access"));
                return;
            }
            WalkieMessageHelper.broadcastMessage(server, sender, frequency, event.getRawText());
            return;
        }

        com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity nearbyWalkie = WalkieMessageHelper.findNearbyActiveBlock(sender);
        if (nearbyWalkie != null) {
            WalkieMessageHelper.broadcastMessage(server, sender, nearbyWalkie.getFrequency(), event.getRawText());
            return;
        }

        Component formattedMessage = Component.translatable("chat.type.text", sender.getDisplayName(), Component.literal(event.getRawText()));
        double currentChatRange = AttractToChatCompat.getEffectiveProximityRange(event.getRawText(), Platform.getHelper().getChatRange());
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

    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer && event.getEntity().getItem().getItem() instanceof WalkieTalkieItem) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            String frequency = WalkieTalkieItem.getFrequency(event.getEntity().getItem());
            if (!frequency.isEmpty()) {
                ConnectionManager.playerDroppedWalkieTalkie(player, frequency);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ConnectionManager.tick(event.getServer());
        }
    }
}
