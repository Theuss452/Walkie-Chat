package com.Theus452.walkietalkie.forge.platform;

import com.Theus452.walkietalkie.forge.config.ForgeModConfigs;
import com.Theus452.walkietalkie.forge.networking.ForgePacketHandler;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import net.minecraftforge.fml.ModList;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public void sendToServer(Object packet) {
        ForgePacketHandler.INSTANCE.sendToServer(packet);
    }

    @Override
    public void sendToClient(Object packet, net.minecraft.server.level.ServerPlayer player) {
        ForgePacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @Override
    public double getChatRange() {
        return ForgeModConfigs.CHAT_RANGE.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
