package com.Theus452.walkietalkie.fabric.platform;

import com.Theus452.walkietalkie.fabric.config.FabricModConfigs;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequencyPayload;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.platform.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricPlatformHelper implements IPlatformHelper {

    
    

    @Override
    public void sendToServer(Object packet) {
        
        if (packet instanceof PacketSetFrequency) {
            PacketSetFrequency p = (PacketSetFrequency) packet;
            
            ClientPlayNetworking.send(new PacketSetFrequencyPayload(p.getNewFrequency(), p.getHand()));
        }
    }

    @Override
    public double getChatRange() {
        
        return FabricModConfigs.getChatRange();
    }
}