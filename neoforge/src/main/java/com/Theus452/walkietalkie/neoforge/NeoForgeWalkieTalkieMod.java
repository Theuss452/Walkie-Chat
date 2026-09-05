package com.Theus452.walkietalkie.neoforge;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.item.ModCreativeModTabs;
import com.Theus452.walkietalkie.item.ModItems;
import com.Theus452.walkietalkie.neoforge.block.NeoForgeBlocks;
import com.Theus452.walkietalkie.neoforge.config.NeoForgeModConfigs;
import com.Theus452.walkietalkie.neoforge.event.NeoForgeEvents;
import com.Theus452.walkietalkie.neoforge.item.NeoForgeCreativeTabs;
import com.Theus452.walkietalkie.neoforge.item.NeoForgeItems;
import com.Theus452.walkietalkie.neoforge.networking.NeoForgePacketHandler;
import com.Theus452.walkietalkie.neoforge.platform.NeoForgePlatformHelper;
import com.Theus452.walkietalkie.neoforge.sounds.NeoForgeSounds;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.proxy.ClientProxy;
import com.Theus452.walkietalkie.proxy.CommonProxy;
import com.Theus452.walkietalkie.proxy.Proxy;
import com.Theus452.walkietalkie.sound.ModSounds;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(WalkieTalkieMod.MOD_ID)
public class NeoForgeWalkieTalkieMod {

    public NeoForgeWalkieTalkieMod(IEventBus modEventBus, ModContainer modContainer) {
        Platform.setHelper(new NeoForgePlatformHelper());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            Proxy.setProxy(new ClientProxy());
        } else {
            Proxy.setProxy(new CommonProxy());
        }

        NeoForgeItems.register(modEventBus);
        NeoForgeBlocks.register(modEventBus);
        NeoForgeSounds.register(modEventBus);
        NeoForgeCreativeTabs.register(modEventBus);

        ModItems.WALKIETALKIE = NeoForgeItems.WALKIETALKIE_REG_OBJ;
        ModSounds.WALKIE_TALKIE_OPEN_MENU = NeoForgeSounds.WALKIE_TALKIE_OPEN_MENU_REG_OBJ;
        ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL = NeoForgeSounds.WALKIE_TALKIE_CHANGE_CHANNEL_REG_OBJ;
        ModSounds.WALKIE_TALKIE_SEND_MSG = NeoForgeSounds.WALKIE_TALKIE_SEND_MSG_REG_OBJ;
        ModSounds.WALKIE_TALKIE_MSG_RECEIVER = NeoForgeSounds.WALKIE_TALKIE_MSG_RECEIVER_REG_OBJ;
        ModCreativeModTabs.WALKIETALKIE_TAB = NeoForgeCreativeTabs.WALKIETALKIE_TAB;

        modContainer.registerConfig(ModConfig.Type.SERVER, NeoForgeModConfigs.SPEC);

        modEventBus.addListener(this::addCreative);
        modEventBus.register(NeoForgePacketHandler.class);
        
        NeoForge.EVENT_BUS.register(new NeoForgeEvents());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.WALKIETALKIE.get());
        }
    }
}
