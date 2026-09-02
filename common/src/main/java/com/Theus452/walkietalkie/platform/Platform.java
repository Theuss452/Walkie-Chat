package com.Theus452.walkietalkie.platform;

import java.util.ServiceLoader;

public class Platform {
    public static final IPlatformHelper HELPER = load();

    private static IPlatformHelper load() {
        return ServiceLoader.load(IPlatformHelper.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma plataforma compatível (Forge ou Fabric) foi encontrada."));
    }

    public static IPlatformHelper getHelper() {
        return HELPER;
    }
}