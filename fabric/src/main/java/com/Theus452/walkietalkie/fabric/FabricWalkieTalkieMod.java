package com.Theus452.walkietalkie.fabric;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.fabric.command.FabricCommands;
import com.Theus452.walkietalkie.fabric.config.FabricModConfigs;
import com.Theus452.walkietalkie.fabric.event.FabricEvents;
import com.Theus452.walkietalkie.fabric.item.FabricCreativeTabs;
import com.Theus452.walkietalkie.fabric.networking.FabricPacketHandler;
import com.Theus452.walkietalkie.fabric.item.FabricItems;
import com.Theus452.walkietalkie.fabric.sounds.FabricSounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricWalkieTalkieMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(WalkieTalkieMod.MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Walkie-Talkie Mod para Fabric...");

        FabricItems.register();
        FabricSounds.register();
        FabricCreativeTabs.register();
        FabricPacketHandler.registerC2SPackets();
        FabricEvents.register();
        FabricCommands.register();
        FabricModConfigs.register();
    }
}