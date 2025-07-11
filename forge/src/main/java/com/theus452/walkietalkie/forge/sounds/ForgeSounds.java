package com.theus452.walkietalkie.forge.sounds;

import com.theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgeSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, WalkieTalkieMod.MOD_ID);

    public static final RegistryObject<SoundEvent> WALKIE_TALKIE_OPEN_MENU_REG_OBJ = SOUND_EVENTS.register("walkie_talkie_open_menu",

            () -> new SoundEvent(new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_talkie_open_menu")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}