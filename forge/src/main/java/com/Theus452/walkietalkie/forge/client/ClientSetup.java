package com.Theus452.walkietalkie.forge.client;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = WalkieTalkieMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {

        event.register(new ResourceLocation(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d"));
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();

        ResourceLocation jsonKey = new ResourceLocation(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d");

        ModelResourceLocation mixinKey = new ModelResourceLocation(new ResourceLocation(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d"), "inventory");

        BakedModel customModel = models.get(jsonKey);
        if (customModel != null) {
            models.put(mixinKey, customModel);
        }
    }
}