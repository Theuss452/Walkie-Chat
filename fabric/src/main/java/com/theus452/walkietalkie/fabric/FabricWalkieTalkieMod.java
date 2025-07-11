package com.theus452.walkietalkie.fabric;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.fabric.command.FabricCommands;
import com.theus452.walkietalkie.fabric.config.FabricModConfigs;
import com.theus452.walkietalkie.fabric.item.FabricCreativeTabs;
import com.theus452.walkietalkie.fabric.networking.FabricPacketHandler;
import com.theus452.walkietalkie.fabric.item.FabricItems;
import com.theus452.walkietalkie.fabric.sounds.FabricSounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricWalkieTalkieMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(WalkieTalkieMod.MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Walkie-Talkie Mod for Fabric...");

        FabricCreativeTabs.register();


        FabricItems.register();
        FabricSounds.register();
        FabricPacketHandler.registerC2SPackets();
        FabricCommands.register();
        FabricModConfigs.register();

    }
}