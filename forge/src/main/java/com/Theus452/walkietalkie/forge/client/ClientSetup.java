package com.Theus452.walkietalkie.forge.client;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.item.WalkieTalkieScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = WalkieTalkieMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static void openWalkieTalkieScreen(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new WalkieTalkieScreen(hand));
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d"));
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();

        ResourceLocation jsonKey = new ResourceLocation(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d");
        ModelResourceLocation mixinKey = new ModelResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_talkie3d", "inventory");

        BakedModel customModel = models.get(jsonKey);
        if (customModel != null) {
            models.put(mixinKey, customModel);
        }
    }
}