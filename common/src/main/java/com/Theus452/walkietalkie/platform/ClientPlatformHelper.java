package com.Theus452.walkietalkie.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.InteractionHand;

public class ClientPlatformHelper {
    @ExpectPlatform
    public static void openWalkieTalkieScreen(InteractionHand hand) {
        throw new AssertionError();
    }
}