package com.Theus452.walkietalkie.platform;

public class Platform {
    private static IPlatformHelper helper;

    public static IPlatformHelper getHelper() {
        if (helper == null) {
            throw new IllegalStateException("Nenhuma plataforma compatível (Forge ou Fabric) foi encontrada.");
        }
        return helper;
    }

    public static void setHelper(IPlatformHelper platformHelper) {
        if (helper != null) {
            throw new IllegalStateException("A plataforma já foi definida!");
        }
        helper = platformHelper;
    }
}