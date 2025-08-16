package com.Theus452.walkietalkie.item;

import com.Theus452.walkietalkie.proxy.Proxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WalkieTalkieItem extends Item {

    public WalkieTalkieItem(Properties pProperties) {
        super(pProperties);
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
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("frequency", frequency);
    }

    public static String getFrequency(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("frequency")) {
            return stack.getTag().getString("frequency");
        }
        return "";
    }
}