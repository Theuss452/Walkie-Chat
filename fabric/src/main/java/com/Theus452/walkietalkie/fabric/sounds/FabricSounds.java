package com.Theus452.walkietalkie.fabric.sounds;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.sound.ModSounds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class FabricSounds {
    public static void register() {
        ResourceLocation id = new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_talkie_open_menu");
        SoundEvent soundEvent = new SoundEvent(id);

        Registry.register(Registry.SOUND_EVENT, id, soundEvent);

        ModSounds.WALKIE_TALKIE_OPEN_MENU = () -> soundEvent;

        ResourceLocation changeChannelId = new ResourceLocation(WalkieTalkieMod.MOD_ID, "change_channel");
        SoundEvent changeChannelEvent = new SoundEvent(changeChannelId);
        Registry.register(Registry.SOUND_EVENT, changeChannelId, changeChannelEvent);

        ResourceLocation sendMsgId = new ResourceLocation(WalkieTalkieMod.MOD_ID, "send_msg");
        SoundEvent sendMsgEvent = new SoundEvent(sendMsgId);
        Registry.register(Registry.SOUND_EVENT, sendMsgId, sendMsgEvent);

        ResourceLocation msgReceiverId = new ResourceLocation(WalkieTalkieMod.MOD_ID, "msg_receiver");
        SoundEvent msgReceiverEvent = new SoundEvent(msgReceiverId);
        Registry.register(Registry.SOUND_EVENT, msgReceiverId, msgReceiverEvent);

        ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL = () -> changeChannelEvent;
        ModSounds.WALKIE_TALKIE_SEND_MSG = () -> sendMsgEvent;
        ModSounds.WALKIE_TALKIE_MSG_RECEIVER = () -> msgReceiverEvent;
    }
}