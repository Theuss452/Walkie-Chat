package com.Theus452.walkietalkie.neoforge.client;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.item.WalkieTalkieScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = WalkieTalkieMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NeoForgeClientSetup {

    public static void openWalkieTalkieScreen(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new WalkieTalkieScreen(hand));
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d"), "standalone"));
    }
}
