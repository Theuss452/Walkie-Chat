package com.Theus452.walkietalkie.networking;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public class WalkieNetworkHandler {

    public static void sendPushMessage(ServerPlayer receiver, String freq, String senderName, String msg) {
        Platform.getHelper().sendToClient(new PacketPushChatMessage(freq, senderName, msg), receiver);
    }

    public static void sendSyncChannels(ServerPlayer receiver, java.util.List<PacketSyncChannels.ChannelInfo> channels) {
        Platform.getHelper().sendToClient(new PacketSyncChannels(channels), receiver);
    }
}
