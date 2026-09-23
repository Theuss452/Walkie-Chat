package com.Theus452.walkietalkie.forge.event;

import com.Theus452.walkietalkie.forge.commands.ForgeCommands;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.util.ConnectionManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEvents {

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ForgeCommands.register(dispatcher);
    }
    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer && event.getEntity().getItem().getItem() instanceof WalkieTalkieItem) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            String frequency = WalkieTalkieItem.getFrequency(event.getEntity().getItem());
            if (!frequency.isEmpty()) {
                ConnectionManager.playerDroppedWalkieTalkie(player, frequency);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ConnectionManager.tick(event.getServer());
        }
    }
}