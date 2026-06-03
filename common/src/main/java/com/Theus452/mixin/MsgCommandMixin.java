package com.Theus452.mixin;

import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.commands.MsgCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(MsgCommand.class)
public abstract class MsgCommandMixin {

    @ModifyVariable(
            method = "sendMessage",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false,
            require = 0
    )
    private static Collection<ServerPlayer> walkietalkie$filterTargetsNamed(Collection<ServerPlayer> targets, CommandSourceStack source) {
        return walkietalkie$filterTargetsImpl(targets, source);
    }

    @ModifyVariable(
            method = "m_246972_",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false,
            require = 0
    )
    private static Collection<ServerPlayer> walkietalkie$filterTargetsSrg(Collection<ServerPlayer> targets, CommandSourceStack source) {
        return walkietalkie$filterTargetsImpl(targets, source);
    }

    @ModifyVariable(
            method = "method_13462",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false,
            require = 0
    )
    private static Collection<ServerPlayer> walkietalkie$filterTargetsIntermediary(Collection<ServerPlayer> targets, CommandSourceStack source) {
        return walkietalkie$filterTargetsImpl(targets, source);
    }

    private static Collection<ServerPlayer> walkietalkie$filterTargetsImpl(Collection<ServerPlayer> targets, CommandSourceStack source) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) {
            return targets;
        }

        double range = Platform.HELPER.getChatRange();
        double rangeSqr = range * range;
        List<ServerPlayer> nearbyTargets = new ArrayList<>();

        for (ServerPlayer target : targets) {
            if (sender.distanceToSqr(target) <= rangeSqr) {
                nearbyTargets.add(target);
            }
        }

        if (nearbyTargets.isEmpty() && !targets.isEmpty()) {
            sender.sendSystemMessage(Component.translatable("message.walkietalkie.no_one_nearby")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        return nearbyTargets;
    }
}
