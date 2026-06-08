package com.Theus452.walkietalkie.networking;

import com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage;
import com.Theus452.walkietalkie.networking.packet.ChannelActionType;
import com.Theus452.walkietalkie.networking.packet.PacketChannelActionResult;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;
import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.server.level.ServerPlayer;


public class WalkieNetworkHandler {

    public static void sendPushMessage(ServerPlayer receiver, String freq, String senderName, String msg) {
        Platform.getHelper().sendToClient(new PacketPushChatMessage(freq, senderName, msg), receiver);
    }

    public static void sendSyncChannels(ServerPlayer receiver, java.util.List<PacketSyncChannels.ChannelInfo> channels) {
        Platform.getHelper().sendToClient(new PacketSyncChannels(channels), receiver);
    }

    public static void sendChannelActionResult(
            ServerPlayer receiver,
            boolean success,
            String frequency,
            String messageKey,
            ChannelActionType actionType,
            long requestId
    ) {
        Platform.getHelper().sendToClient(
                new PacketChannelActionResult(success, frequency, messageKey, actionType, requestId),
                receiver
        );
    }
}
