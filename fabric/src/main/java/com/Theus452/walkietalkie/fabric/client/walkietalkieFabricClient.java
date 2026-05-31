package com.Theus452.walkietalkie.fabric.client;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.proxy.ClientProxy;
import com.Theus452.walkietalkie.proxy.Proxy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public final class walkietalkieFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Proxy.setProxy(new ClientProxy());

        ModelResourceLocation walkieTalkie3d = new ModelResourceLocation(new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_talkie3d"), "inventory");

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> out.accept(walkieTalkie3d));

        ModelLoadingRegistry.INSTANCE.registerVariantProvider(resourceManager -> (modelIdentifier, modelProviderContext) -> {
            if (modelIdentifier.equals(walkieTalkie3d)) {
                return modelProviderContext.loadModel(new ResourceLocation(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d"));
            }
            return null;
        });

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.Theus452.walkietalkie.fabric.networking.FabricPacketHandler.PUSH_CHAT_MESSAGE_ID, (client, handler, buf, responseSender) -> {
            com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage packet = new com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage(buf);
            client.execute(() -> com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage.handle(packet));
        });

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.Theus452.walkietalkie.fabric.networking.FabricPacketHandler.SYNC_CHANNELS_ID, (client, handler, buf, responseSender) -> {
            com.Theus452.walkietalkie.networking.packet.PacketSyncChannels packet = new com.Theus452.walkietalkie.networking.packet.PacketSyncChannels(buf);
            client.execute(() -> com.Theus452.walkietalkie.networking.packet.PacketSyncChannels.handle(packet));
        });
    }
}