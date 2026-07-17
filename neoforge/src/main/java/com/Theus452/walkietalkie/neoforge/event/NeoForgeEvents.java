package com.Theus452.walkietalkie.neoforge.event;

import com.Theus452.walkietalkie.neoforge.commands.NeoForgeCommands;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.sound.ModSounds;
import com.Theus452.walkietalkie.util.ConnectionManager;
import com.Theus452.walkietalkie.util.IncomingMessageSoundLimiter;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

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

            int senderWalkieTalkieCount = countWalkieTalkies(sender);
            sender.sendSystemMessage(createWalkieTalkieMessage(sender, event.getRawText(), frequency, senderWalkieTalkieCount > 1));
            sender.playNotifySound(ModSounds.WALKIE_TALKIE_SEND_MSG.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

            int receivers = 0;
            for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
                if (receiver == sender) continue;
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
                    int walkieTalkieCount = countWalkieTalkies(receiver);
                    Component messageToSend = createWalkieTalkieMessage(sender, event.getRawText(), frequency, walkieTalkieCount > 1);
                    receiver.sendSystemMessage(messageToSend);
                    IncomingMessageSoundLimiter.SoundDecision soundDecision = IncomingMessageSoundLimiter.evaluate(receiver);
                    if (soundDecision.shouldPlay()) {
                        receiver.playNotifySound(ModSounds.WALKIE_TALKIE_MSG_RECEIVER.get(), SoundSource.PLAYERS, soundDecision.volume(), soundDecision.pitch());
                    }
                    receivers++;
                }
            }

            if (receivers == 0) {
                sender.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_on_frequency").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        } else {
            Component formattedMessage = Component.translatable("chat.type.text", sender.getDisplayName(), Component.literal(event.getRawText()));
            double currentChatRange = Platform.getHelper().getChatRange();
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

    private static int countWalkieTalkies(ServerPlayer player) {
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
