package com.Theus452.walkietalkie.networking.packet;

import com.Theus452.walkietalkie.channel.ChannelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class PacketBlockChannelAction {
    private final BlockPos pos;
    private final ChannelActionType actionType;
    private final String frequency;
    private final String name;
    private final boolean passwordProtected;
    private final String password;
    private final long requestId;

    public PacketBlockChannelAction(BlockPos pos, ChannelActionType actionType, String frequency, String name, long requestId) {
        this(pos, actionType, frequency, name, false, "", requestId);
    }

    public PacketBlockChannelAction(BlockPos pos, ChannelActionType actionType, String frequency, String name, boolean passwordProtected, String password, long requestId) {
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.actionType = actionType == null ? ChannelActionType.JOIN : actionType;
        this.frequency = frequency == null ? "" : frequency;
        this.name = name == null ? "" : name;
        this.passwordProtected = passwordProtected;
        this.password = password == null ? "" : password;
        this.requestId = requestId;
    }

    public PacketBlockChannelAction(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        actionType = buffer.readEnum(ChannelActionType.class);
        frequency = buffer.readUtf(3);
        name = buffer.readUtf(24);
        passwordProtected = buffer.readBoolean();
        password = buffer.readUtf(32);
        requestId = buffer.readVarLong();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(actionType);
        buffer.writeUtf(frequency, 3);
        buffer.writeUtf(name, 24);
        buffer.writeBoolean(passwordProtected);
        buffer.writeUtf(password, 32);
        buffer.writeVarLong(requestId);
    }

    public static void handle(PacketBlockChannelAction packet, ServerPlayer player) {
        if (packet == null || player == null) {
            return;
        }
        switch (packet.actionType) {
            case CREATE -> ChannelManager.createBlockChannel(player, packet.pos, packet.frequency, packet.name, packet.passwordProtected, packet.password, packet.requestId);
            case JOIN -> ChannelManager.joinBlockChannel(player, packet.pos, packet.frequency, packet.password.isEmpty() ? packet.name : packet.password, packet.requestId);
            case LEAVE -> ChannelManager.leaveBlockChannel(player, packet.pos, packet.requestId);
            case DISCONNECT_ALL -> ChannelManager.disconnectAllOwnedBlocks(player, packet.frequency, packet.requestId);
        }
    }
}
