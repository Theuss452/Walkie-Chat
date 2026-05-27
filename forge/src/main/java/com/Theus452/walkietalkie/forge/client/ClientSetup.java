package com.Theus452.walkietalkie.forge.client;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.item.WalkieTalkieScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = WalkieTalkieMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static void openWalkieTalkieScreen(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new WalkieTalkieScreen(hand));
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d"), "standalone"));
    }
}
