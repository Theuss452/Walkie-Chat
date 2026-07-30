package com.Theus452.walkietalkie.forge.networking;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.networking.packet.PacketChannelActionResult;
import com.Theus452.walkietalkie.networking.packet.PacketCreateChannel;
import com.Theus452.walkietalkie.networking.packet.PacketJoinChannel;
import com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage;
import com.Theus452.walkietalkie.networking.packet.PacketRequestChannels;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;
import com.Theus452.walkietalkie.networking.packet.PacketKickPlayer;
import com.Theus452.walkietalkie.networking.packet.PacketRenameChannel;
import com.Theus452.walkietalkie.networking.packet.C2S_ToggleBlockRelayPacket;
import com.Theus452.walkietalkie.networking.packet.C2S_WalkieBlockMessagePacket;
import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class ForgePacketHandler {
    private static final String PROTOCOL_VERSION = "4";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WalkieTalkieMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ForgePacketHandler() {
    }

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, PacketSetFrequency.class, PacketSetFrequency::toBytes, PacketSetFrequency::new, ForgePacketHandler::handleSetFrequency);
        INSTANCE.registerMessage(id++, PacketCreateChannel.class, PacketCreateChannel::toBytes, PacketCreateChannel::new, ForgePacketHandler::handleCreateChannel);
        INSTANCE.registerMessage(id++, PacketJoinChannel.class, PacketJoinChannel::toBytes, PacketJoinChannel::new, ForgePacketHandler::handleJoinChannel);
        INSTANCE.registerMessage(id++, PacketRequestChannels.class, PacketRequestChannels::toBytes, PacketRequestChannels::new, ForgePacketHandler::handleRequestChannels);
        INSTANCE.registerMessage(id++, PacketPushChatMessage.class, PacketPushChatMessage::toBytes, PacketPushChatMessage::new, ForgePacketHandler::handlePushChatMessage);
        INSTANCE.registerMessage(id++, PacketSyncChannels.class, PacketSyncChannels::toBytes, PacketSyncChannels::new, ForgePacketHandler::handleSyncChannels);
        INSTANCE.registerMessage(id++, PacketChannelActionResult.class, PacketChannelActionResult::toBytes, PacketChannelActionResult::new, ForgePacketHandler::handleChannelActionResult);
        INSTANCE.registerMessage(id++, PacketKickPlayer.class, PacketKickPlayer::toBytes, PacketKickPlayer::new, ForgePacketHandler::handleKickPlayer);
        INSTANCE.registerMessage(id++, PacketRenameChannel.class, PacketRenameChannel::toBytes, PacketRenameChannel::new, ForgePacketHandler::handleRenameChannel);
        INSTANCE.registerMessage(id++, C2S_WalkieBlockMessagePacket.class, C2S_WalkieBlockMessagePacket::toBytes, C2S_WalkieBlockMessagePacket::new, ForgePacketHandler::handleWalkieBlockMessage);
        INSTANCE.registerMessage(id++, PacketSetBlockFrequency.class, PacketSetBlockFrequency::toBytes, PacketSetBlockFrequency::new, ForgePacketHandler::handleSetBlockFrequency);
        INSTANCE.registerMessage(id, C2S_ToggleBlockRelayPacket.class, C2S_ToggleBlockRelayPacket::toBytes, C2S_ToggleBlockRelayPacket::new, ForgePacketHandler::handleToggleBlockRelay);
    }

    private static void handleSetFrequency(PacketSetFrequency packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PacketSetFrequency.handle(packet, player);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleCreateChannel(PacketCreateChannel packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PacketCreateChannel.handle(packet, player);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleJoinChannel(PacketJoinChannel packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PacketJoinChannel.handle(packet, player);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleRequestChannels(PacketRequestChannels packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PacketRequestChannels.handle(packet, player);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handlePushChatMessage(PacketPushChatMessage packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> PacketPushChatMessage.handle(packet));
        context.setPacketHandled(true);
    }

    private static void handleSyncChannels(PacketSyncChannels packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> PacketSyncChannels.handle(packet));
        context.setPacketHandled(true);
    }

    private static void handleChannelActionResult(PacketChannelActionResult packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> PacketChannelActionResult.handle(packet));
        context.setPacketHandled(true);
    }

    private static void handleKickPlayer(PacketKickPlayer packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PacketKickPlayer.handle(packet, player);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleRenameChannel(PacketRenameChannel packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PacketRenameChannel.handle(packet, player);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleWalkieBlockMessage(C2S_WalkieBlockMessagePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> C2S_WalkieBlockMessagePacket.handle(packet, context.getSender()));
        context.setPacketHandled(true);
    }

    private static void handleSetBlockFrequency(PacketSetBlockFrequency packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> PacketSetBlockFrequency.handle(packet, context.getSender()));
        context.setPacketHandled(true);
    }

    private static void handleToggleBlockRelay(C2S_ToggleBlockRelayPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> C2S_ToggleBlockRelayPacket.handle(packet, context.getSender()));
        context.setPacketHandled(true);
    }
}
