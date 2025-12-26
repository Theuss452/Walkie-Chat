package com.Theus452.walkietalkie.forge.event;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.forge.commands.ForgeCommands;
import com.Theus452.walkietalkie.util.ConnectionManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WalkieTalkieMod.MOD_ID)
public class ForgeEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ForgeCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ConnectionManager.tick(event.getServer());
        }
    }
}