package com.theus452.walkietalkie.fabric.platform;

import com.theus452.walkietalkie.fabric.config.FabricModConfigs;
import com.theus452.walkietalkie.fabric.networking.FabricPacketHandler;
import com.theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.theus452.walkietalkie.platform.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class FabricPlatformHelper implements IPlatformHelper {

    
    

    @Override
    public void sendToServer(Object packet) {
        
        if (packet instanceof PacketSetFrequency) {
            PacketSetFrequency p = (PacketSetFrequency) packet;
            
            FriendlyByteBuf buf = PacketByteBufs.create();
            
            p.toBytes(buf);
            
            ClientPlayNetworking.send(FabricPacketHandler.SET_FREQUENCY_ID, buf);
        }
    }

    @Override
    public double getChatRange() {
        
        return FabricModConfigs.getChatRange();
    }
}