package com.Theus452.mixin;

import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.commands.MsgCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(MsgCommand.class)
public abstract class MsgCommandMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private static void walkietalkie$applyProximityToTell(CommandSourceStack source, Collection<ServerPlayer> targets, PlayerChatMessage message, CallbackInfo ci) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) {
            return;
        }

        double range = Platform.getHelper().getChatRange();
        double rangeSqr = range * range;
        List<ServerPlayer> nearbyTargets = new ArrayList<>();

        for (ServerPlayer target : targets) {
            if (sender.distanceToSqr(target) <= rangeSqr) {
                nearbyTargets.add(target);
            }
        }

        if (nearbyTargets.isEmpty()) {
            sender.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            ci.cancel();
            return;
        }

        ChatType.Bound incoming = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, source);
        OutgoingChatMessage outgoing = OutgoingChatMessage.create(message);
        boolean anyFiltered = false;

        for (ServerPlayer target : nearbyTargets) {
            ChatType.Bound outgoingBound = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, source)
                    .withTargetName(target.getDisplayName());
            source.sendChatMessage(outgoing, false, outgoingBound);
            boolean filtered = source.shouldFilterMessageTo(target);
            target.sendChatMessage(outgoing, filtered, incoming);
            anyFiltered |= filtered && message.isFullyFiltered();
        }

        if (anyFiltered) {
            source.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }

        ci.cancel();
    }
}
