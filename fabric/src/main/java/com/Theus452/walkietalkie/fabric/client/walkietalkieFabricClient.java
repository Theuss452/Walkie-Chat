package com.Theus452.walkietalkie.fabric.client;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.fabric.networking.FabricPacketHandler;
import com.Theus452.walkietalkie.networking.packet.PacketChannelActionResult;
import com.Theus452.walkietalkie.networking.packet.PacketPushChatMessage;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;
import com.Theus452.walkietalkie.proxy.ClientProxy;
import com.Theus452.walkietalkie.proxy.Proxy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

        ClientPlayNetworking.registerGlobalReceiver(FabricPacketHandler.PUSH_CHAT_MESSAGE_ID, (client, handler, buf, responseSender) -> {
            PacketPushChatMessage packet = new PacketPushChatMessage(buf);
            client.execute(() -> PacketPushChatMessage.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricPacketHandler.SYNC_CHANNELS_ID, (client, handler, buf, responseSender) -> {
            PacketSyncChannels packet = new PacketSyncChannels(buf);
            client.execute(() -> PacketSyncChannels.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricPacketHandler.CHANNEL_ACTION_RESULT_ID, (client, handler, buf, responseSender) -> {
            PacketChannelActionResult packet = new PacketChannelActionResult(buf);
            client.execute(() -> PacketChannelActionResult.handle(packet));
        });
    }
}
