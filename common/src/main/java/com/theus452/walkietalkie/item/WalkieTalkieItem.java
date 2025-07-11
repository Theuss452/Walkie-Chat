package com.theus452.walkietalkie.item;

import com.theus452.walkietalkie.sound.ModSounds;
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
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        if (pLevel.isClientSide()) {
            ClientOnly.openScreen(pUsedHand);
        }

        return InteractionResultHolder.success(itemStack);
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

    private static class ClientOnly {
        private static void openScreen(InteractionHand hand) {

            net.minecraft.client.Minecraft.getInstance().setScreen(new WalkieTalkieScreen(hand));
            net.minecraft.client.resources.sounds.SimpleSoundInstance sound = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(ModSounds.WALKIE_TALKIE_OPEN_MENU.get(), 1.0F);
            net.minecraft.client.Minecraft.getInstance().getSoundManager().play(sound);
        }
    }
}