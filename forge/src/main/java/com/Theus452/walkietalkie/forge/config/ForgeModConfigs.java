package com.Theus452.walkietalkie.forge.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeModConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue CHAT_RANGE;

    static {
        BUILDER.push("Walkie-Talkie Settings");

        CHAT_RANGE = BUILDER.comment("The maximum distance (in blocks) a player can be heard from without a walkie-talkie.")
                .defineInRange("chatDistance", 35.0, 0.0, Double.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}