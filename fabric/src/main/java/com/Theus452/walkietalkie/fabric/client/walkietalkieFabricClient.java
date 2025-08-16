package com.Theus452.walkietalkie.fabric.client;

import com.Theus452.walkietalkie.proxy.ClientProxy;
import com.Theus452.walkietalkie.proxy.Proxy;
import net.fabricmc.api.ClientModInitializer;

public final class walkietalkieFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Proxy.setProxy(new ClientProxy());
    }
}