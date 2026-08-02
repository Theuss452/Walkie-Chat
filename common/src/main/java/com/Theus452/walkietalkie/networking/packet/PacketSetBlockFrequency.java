package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.block.WalkieTalkieBlock;
import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.sound.ModSounds;
import com.Theus452.walkietalkie.util.ServerRateLimiter;
import com.Theus452.walkietalkie.util.WalkieFrequency;
import com.Theus452.walkietalkie.util.WalkieSecurity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class PacketSetBlockFrequency {
    private static final long FREQUENCY_CHANGE_INTERVAL_TICKS = 10L;

    private final String frequency;
    private final BlockPos pos;

    public PacketSetBlockFrequency(String frequency, BlockPos pos) {
        this.frequency = frequency == null ? "" : frequency;
        this.pos = pos == null ? BlockPos.ZERO : pos;
    }

    public PacketSetBlockFrequency(FriendlyByteBuf buf) {
        this.frequency = buf.readUtf(10);
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(frequency == null ? "" : frequency, 10);
        buf.writeBlockPos(pos == null ? BlockPos.ZERO : pos);
    }

    public static void handle(PacketSetBlockFrequency packet, ServerPlayer player) {
        if (!ServerRateLimiter.allow(player, "walkietalkie:block_frequency", FREQUENCY_CHANGE_INTERVAL_TICKS)) return;
        String frequency = WalkieSecurity.sanitizeFrequency(packet.frequency);
        if (frequency.isEmpty()) return;
        if (WalkieFrequency.isPrivate(frequency)) {
            player.sendSystemMessage(Component.translatable("message.walkietalkie.block.private_forbidden").withStyle(ChatFormatting.RED));
            return;
        }
        WalkieTalkieBlockEntity blockEntity = WalkieSecurity.interactableWalkieBlock(player, packet.pos);
        if (blockEntity == null) return;
        if (!WalkieSecurity.canControlWalkieBlock(player, blockEntity)) {
            player.sendSystemMessage(Component.translatable("message.walkietalkie.block.not_owner").withStyle(ChatFormatting.RED));
            return;
        }
        blockEntity.setFrequency(frequency);
        blockEntity.setActive(true);
        if (player.level() != null) {
            player.level().setBlock(packet.pos, blockEntity.getBlockState().setValue(WalkieTalkieBlock.ACTIVE, true), 3);
            if (ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL != null) {
                player.level().playSound(null, packet.pos, ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        if (blockEntity.ownerUUID == null) {
            blockEntity.setOwnerUUID(player.getUUID());
        }
        player.sendSystemMessage(Component.translatable("message.walkietalkie.join.self", frequency).withStyle(ChatFormatting.GREEN));
    }
}


