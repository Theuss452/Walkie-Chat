package com.Theus452.walkietalkie.util;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public final class WalkieSecurity {
    public static final double BLOCK_INTERACTION_RANGE_SQ = 64.0D;

    private WalkieSecurity() {
    }

    public static boolean isUsablePlayer(ServerPlayer player) {
        return player != null && player.isAlive() && !player.isSpectator();
    }

    public static String sanitizeFrequency(String raw) {
        return WalkieFrequency.sanitize(raw);
    }

    public static String sanitizeMessage(String raw) {
        return WalkieFrequency.sanitizeChatMessage(raw);
    }

    public static ItemStack walkieInHand(ServerPlayer player, InteractionHand hand) {
        if (player == null || hand == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = player.getItemInHand(hand);
        return stack.getItem() instanceof WalkieTalkieItem ? stack : ItemStack.EMPTY;
    }

    public static boolean isCloseEnough(ServerPlayer player, BlockPos pos) {
        return player != null && pos != null
            && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= BLOCK_INTERACTION_RANGE_SQ;
    }

    public static WalkieTalkieBlockEntity interactableWalkieBlock(ServerPlayer player, BlockPos pos) {
        if (!isUsablePlayer(player) || !isCloseEnough(player, pos)) {
            return null;
        }
        BlockEntity be = player.level().getBlockEntity(pos);
        return be instanceof WalkieTalkieBlockEntity walkie ? walkie : null;
    }

    public static boolean canControlWalkieBlock(ServerPlayer player, WalkieTalkieBlockEntity block) {
        if (!isUsablePlayer(player) || block == null) return false;
        if (player.hasPermissions(2)) return true;
        UUID owner = block.ownerUUID;
        return owner == null || owner.equals(player.getUUID());
    }
}
