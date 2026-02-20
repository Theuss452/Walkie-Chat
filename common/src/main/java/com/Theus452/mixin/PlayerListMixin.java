package com.Theus452.mixin;

import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.List;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private List<ServerPlayer> players;

    @Inject(
            method = "broadcastChatMessage",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void walkietalkie$applyProximityToCommandChatNamed(PlayerChatMessage message, CommandSourceStack sender, ChatType.Bound boundChatType, CallbackInfo ci) {
        walkietalkie$applyProximityToCommandChatImpl(message, sender, boundChatType, ci);
    }

    @Inject(
            method = "m_243063_",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void walkietalkie$applyProximityToCommandChatSrg(PlayerChatMessage message, CommandSourceStack sender, ChatType.Bound boundChatType, CallbackInfo ci) {
        walkietalkie$applyProximityToCommandChatImpl(message, sender, boundChatType, ci);
    }

    @Inject(
            method = "method_44166",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void walkietalkie$applyProximityToCommandChatIntermediary(PlayerChatMessage message, CommandSourceStack sender, ChatType.Bound boundChatType, CallbackInfo ci) {
        walkietalkie$applyProximityToCommandChatImpl(message, sender, boundChatType, ci);
    }

    private void walkietalkie$applyProximityToCommandChatImpl(PlayerChatMessage message, CommandSourceStack sender, ChatType.Bound boundChatType, CallbackInfo ci) {
        ServerPlayer senderPlayer = sender.getPlayer();
        if (senderPlayer == null) {
            return;
        }

        if (!isProximityCommandChatType(sender, boundChatType)) {
            return;
        }

        double range = Platform.getHelper().getChatRange();
        double rangeSqr = range * range;

        boolean trusted = message.hasSignature() && !message.hasExpiredServer(Instant.now());
        this.server.logChatMessage(message.decoratedContent(), boundChatType, trusted ? null : "Not Secure");
        OutgoingChatMessage outgoingMessage = OutgoingChatMessage.create(message);
        boolean anyFiltered = false;
        int recipientsFound = 0;

        for (ServerPlayer recipient : this.players) {
            if (senderPlayer.distanceToSqr(recipient) > rangeSqr) {
                continue;
            }

            boolean filtered = sender.shouldFilterMessageTo(recipient);
            recipient.sendChatMessage(outgoingMessage, filtered, boundChatType);
            anyFiltered |= filtered && message.isFullyFiltered();
            recipientsFound++;
        }

        if (anyFiltered) {
            senderPlayer.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }

        if (recipientsFound <= 1 && this.players.size() > 1) {
            senderPlayer.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        ci.cancel();
    }

    private static boolean isProximityCommandChatType(CommandSourceStack sender, ChatType.Bound boundChatType) {
        Registry<ChatType> chatTypes = sender.registryAccess().registryOrThrow(Registries.CHAT_TYPE);
        String boundTypeKey = boundChatType.chatType().chat().translationKey();
        String sayTypeKey = chatTypes.getOrThrow(ChatType.SAY_COMMAND).chat().translationKey();
        String emoteTypeKey = chatTypes.getOrThrow(ChatType.EMOTE_COMMAND).chat().translationKey();
        return boundTypeKey.equals(sayTypeKey) || boundTypeKey.equals(emoteTypeKey);
    }
}
