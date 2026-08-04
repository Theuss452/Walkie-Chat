package com.Theus452.walkietalkie.item;

import com.Theus452.walkietalkie.block.ModBlocks;
import com.Theus452.walkietalkie.block.WalkieTalkieBlock;
import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.proxy.Proxy;
import com.Theus452.walkietalkie.sound.ModSounds;
import com.Theus452.walkietalkie.util.SafeNbt;
import com.Theus452.walkietalkie.util.WalkieFrequency;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;
import java.util.UUID;

public class WalkieTalkieItem extends Item {
    public static final String FREQUENCY_KEY = "frequency";
    public static final String CHANNEL_NAME_KEY = "channel_name";
    public static final String BLOCK_OWNER_KEY = "block_owner";
    public static final String BLOCK_RELAY_KEY = "block_relay_enabled";

    public WalkieTalkieItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown() && ModBlocks.WALKIE_TALKIE_BLOCK != null) {
            Level level = context.getLevel();
            BlockPlaceContext placeContext = new BlockPlaceContext(context);
            if (!placeContext.canPlace()) {
                return InteractionResult.FAIL;
            }

            BlockState blockState = ModBlocks.WALKIE_TALKIE_BLOCK.get().getStateForPlacement(placeContext);
            if (blockState == null) {
                return InteractionResult.FAIL;
            }

            BlockPos placePos = placeContext.getClickedPos();
            ItemStack stack = context.getItemInHand();

            CollisionContext collisionContext = CollisionContext.of(player);
            if (!level.isUnobstructed(blockState, placePos, collisionContext)) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                level.setBlock(placePos, blockState, 3);
                BlockEntity be = level.getBlockEntity(placePos);
                if (be instanceof WalkieTalkieBlockEntity walkieBE) {
                    WalkieTalkieBlock.applyStackDataToBlock(walkieBE, stack, player);
                }

                if (ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL != null) {
                    level.playSound(null, placePos, ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), SoundSource.BLOCKS, 0.8f, 0.85f);
                }

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            Proxy.proxy.openWalkieTalkieScreen(hand);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    public static void setFrequency(ItemStack stack, String frequency) {
        SafeNbt.putString(stack, FREQUENCY_KEY, WalkieFrequency.sanitize(frequency), WalkieFrequency.MAX_WIRE_LENGTH);
    }

    public static String getFrequency(ItemStack stack) {
        return SafeNbt.existing(stack)
            .map(tag -> SafeNbt.string(tag, FREQUENCY_KEY, "", WalkieFrequency.MAX_WIRE_LENGTH))
            .orElse("");
    }

    public static String getChannelName(ItemStack stack) {
        return SafeNbt.existing(stack)
            .map(tag -> SafeNbt.string(tag, CHANNEL_NAME_KEY, "", 20))
            .orElse("");
    }

    public static void setChannelName(ItemStack stack, String name) {
        SafeNbt.putString(stack, CHANNEL_NAME_KEY, WalkieFrequency.sanitizeChannelName(name), 20);
    }

    public static UUID getBlockOwner(ItemStack stack) {
        return SafeNbt.existing(stack)
            .map(tag -> SafeNbt.uuidOrNull(tag, BLOCK_OWNER_KEY))
            .orElse(null);
    }

    public static void setBlockOwner(ItemStack stack, UUID owner) {
        SafeNbt.putUuid(stack, BLOCK_OWNER_KEY, owner);
    }

    public static boolean getBlockRelayEnabled(ItemStack stack) {
        return SafeNbt.existing(stack)
            .map(tag -> SafeNbt.bool(tag, BLOCK_RELAY_KEY, true))
            .orElse(true);
    }

    public static void setBlockRelayEnabled(ItemStack stack, boolean relayEnabled) {
        SafeNbt.putBoolean(stack, BLOCK_RELAY_KEY, relayEnabled);
    }
}