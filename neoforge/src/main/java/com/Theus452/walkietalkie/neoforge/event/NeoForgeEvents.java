package com.Theus452.walkietalkie.neoforge.event;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.neoforge.commands.NeoForgeCommands;
import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
import com.Theus452.walkietalkie.util.ConnectionManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class NeoForgeEvents {

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        NeoForgeCommands.register(dispatcher);
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (event.getEntity().getOwner() instanceof ServerPlayer player && event.getEntity().getItem().getItem() instanceof WalkieTalkieItem) {
            String frequency = WalkieTalkieItem.getFrequency(event.getEntity().getItem());
            if (!frequency.isEmpty()) {
                ConnectionManager.playerDroppedWalkieTalkie(player, frequency);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        ConnectionManager.tick(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        WalkieBlockRegistry.clearOnStop();
    }
}
