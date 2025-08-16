package com.Theus452.walkietalkie.proxy;

import com.Theus452.walkietalkie.item.WalkieTalkieScreen;
import com.Theus452.walkietalkie.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public class ClientProxy extends CommonProxy {
    @Override
    public void openWalkieTalkieScreen(InteractionHand hand) {
        Minecraft.getInstance().player.playSound(ModSounds.WALKIE_TALKIE_OPEN_MENU.get(), 1.0F, 1.0F);
        Minecraft.getInstance().setScreen(new WalkieTalkieScreen(hand));
    }
}