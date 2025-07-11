package com.theus452.walkietalkie.forge.event;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.forge.commands.ForgeCommands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WalkieTalkieMod.MOD_ID)

public class ForgeEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ForgeCommands.register(event.getDispatcher());
    }

}