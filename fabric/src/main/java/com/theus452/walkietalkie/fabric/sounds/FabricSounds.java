package com.theus452.walkietalkie.fabric.sounds;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.sound.ModSounds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class FabricSounds {
    public static void register() {
        ResourceLocation id = new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_talkie_open_menu");
        SoundEvent soundEvent = new SoundEvent(id);

        Registry.register(Registry.SOUND_EVENT, id, soundEvent);

        ModSounds.WALKIE_TALKIE_OPEN_MENU = () -> soundEvent;
    }
}