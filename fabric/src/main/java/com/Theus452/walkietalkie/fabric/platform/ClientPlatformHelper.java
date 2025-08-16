package com.Theus452.walkietalkie.fabric.platform;

import com.Theus452.walkietalkie.item.WalkieTalkieScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public class ClientPlatformHelper {
    public static void openWalkieTalkieScreen(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new WalkieTalkieScreen(hand));
    }
}