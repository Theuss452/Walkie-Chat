package com.theus452.walkietalkie.fabric.client;

import com.theus452.walkietalkie.proxy.ClientProxy;
import com.theus452.walkietalkie.proxy.Proxy;
import net.fabricmc.api.ClientModInitializer;

final class walkietalkieFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Proxy.setProxy(new ClientProxy());
    }
}