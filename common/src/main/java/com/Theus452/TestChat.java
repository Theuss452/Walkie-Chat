package com.Theus452;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

public class TestChat {
    public void test(ServerPlayer target, PlayerChatMessage msg, CommandSourceStack sender, ChatType.Bound bound) {
        target.sendChatMessage(msg, sender, bound);
    }
}
