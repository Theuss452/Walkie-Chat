package com.Theus452.walkietalkie.forge.sounds;

import com.Theus452.walkietalkie.WalkieTalkieMod;
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
    public static final RegistryObject<SoundEvent> WALKIE_TALKIE_CHANGE_CHANNEL_REG_OBJ = SOUND_EVENTS.register("change_channel",
            () -> new SoundEvent(new ResourceLocation(WalkieTalkieMod.MOD_ID, "change_channel")));
    public static final RegistryObject<SoundEvent> WALKIE_TALKIE_SEND_MSG_REG_OBJ = SOUND_EVENTS.register("send_msg",
            () -> new SoundEvent(new ResourceLocation(WalkieTalkieMod.MOD_ID, "send_msg")));
    public static final RegistryObject<SoundEvent> WALKIE_TALKIE_MSG_RECEIVER_REG_OBJ = SOUND_EVENTS.register("msg_receiver",
            () -> new SoundEvent(new ResourceLocation(WalkieTalkieMod.MOD_ID, "msg_receiver")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}