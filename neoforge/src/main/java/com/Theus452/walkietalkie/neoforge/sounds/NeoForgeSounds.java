package com.Theus452.walkietalkie.neoforge.sounds;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NeoForgeSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, WalkieTalkieMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> WALKIE_TALKIE_OPEN_MENU_REG_OBJ = SOUND_EVENTS.register("walkie_talkie_open_menu",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "walkie_talkie_open_menu")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WALKIE_TALKIE_CHANGE_CHANNEL_REG_OBJ = SOUND_EVENTS.register("change_channel",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "change_channel")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WALKIE_TALKIE_SEND_MSG_REG_OBJ = SOUND_EVENTS.register("send_msg",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "send_msg")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WALKIE_TALKIE_MSG_RECEIVER_REG_OBJ = SOUND_EVENTS.register("msg_receiver",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "msg_receiver")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
