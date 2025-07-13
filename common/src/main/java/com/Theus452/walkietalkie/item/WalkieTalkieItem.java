package com.Theus452.walkietalkie.item;

import com.Theus452.walkietalkie.sound.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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
            openScreen(hand);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Environment(EnvType.CLIENT)
    private void openScreen(InteractionHand hand) {
        Minecraft.getInstance().player.playSound(ModSounds.WALKIE_TALKIE_OPEN_MENU.get(), 1.0F, 1.0F);
        Minecraft.getInstance().setScreen(new WalkieTalkieScreen(hand));
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