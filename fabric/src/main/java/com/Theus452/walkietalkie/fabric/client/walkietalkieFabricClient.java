package com.Theus452.walkietalkie.fabric.client;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.proxy.ClientProxy;
import com.Theus452.walkietalkie.proxy.Proxy;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;

public final class walkietalkieFabricClient implements ClientModInitializer {
    private static final ResourceLocation WALKIE_TALKIE_3D_MODEL_ID = ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d");

    @Override
    public void onInitializeClient() {
        Proxy.setProxy(new ClientProxy());
        ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(WALKIE_TALKIE_3D_MODEL_ID));
    }
}
