package com.theus452.walkietalkie.forge;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.forge.config.ForgeModConfigs;
import com.theus452.walkietalkie.forge.event.ForgeEvents;
import com.theus452.walkietalkie.forge.item.ForgeCreativeTabs;
import com.theus452.walkietalkie.forge.item.ForgeItems;
import com.theus452.walkietalkie.forge.networking.ForgePacketHandler;
import com.theus452.walkietalkie.forge.sounds.ForgeSounds;
import com.theus452.walkietalkie.item.ModCreativeModTabs;
import com.theus452.walkietalkie.item.ModItems;
import com.theus452.walkietalkie.proxy.ClientProxy;
import com.theus452.walkietalkie.proxy.CommonProxy;
import com.theus452.walkietalkie.proxy.Proxy;
import com.theus452.walkietalkie.sound.ModSounds;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(WalkieTalkieMod.MOD_ID)
public class ForgeWalkieTalkieMod {

    public ForgeWalkieTalkieMod() {
        if (FMLEnvironment.dist.isClient()) {
            Proxy.setProxy(new ClientProxy());
        } else {
            Proxy.setProxy(new CommonProxy());
        }

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        ForgeItems.register(modEventBus);
        ForgeSounds.register(modEventBus);
        ForgeCreativeTabs.register(modEventBus);


        ModItems.WALKIETALKIE = ForgeItems.WALKIETALKIE_REG_OBJ;
        ModSounds.WALKIE_TALKIE_OPEN_MENU = ForgeSounds.WALKIE_TALKIE_OPEN_MENU_REG_OBJ;
        ModCreativeModTabs.WALKIETALKIE_TAB = ForgeCreativeTabs.WALKIETALKIE_TAB;


        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ForgeModConfigs.SPEC);
        ForgePacketHandler.register();


        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());

    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.WALKIETALKIE.get());
        }


        if (event.getTabKey() == ForgeCreativeTabs.WALKIETALKIE_TAB.getKey()) {
            event.accept(ModItems.WALKIETALKIE.get());
        }
    }
}