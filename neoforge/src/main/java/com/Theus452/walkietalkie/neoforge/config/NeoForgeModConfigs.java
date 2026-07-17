package com.Theus452.walkietalkie.neoforge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeModConfigs {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue CHAT_RANGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("Walkie-Talkie Settings");
        CHAT_RANGE = builder
                .comment("Range in blocks where players can hear others without being on the same frequency.")
                .defineInRange("chatRange", 20.0, 1.0, 10000.0);
        builder.pop();
        SPEC = builder.build();
    }
}
