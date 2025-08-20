package com.Theus452.walkietalkie.fabric.client;

import com.Theus452.walkietalkie.WalkieTalkieMod;;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public final class walkietalkieFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ModelResourceLocation walkieTalkie3d = new ModelResourceLocation(new ResourceLocation(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d"), "inventory");


        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> out.accept(walkieTalkie3d));


        ModelLoadingRegistry.INSTANCE.registerVariantProvider(resourceManager -> (modelIdentifier, modelProviderContext) -> {
            if (modelIdentifier.equals(walkieTalkie3d)) {
                return modelProviderContext.loadModel(new ResourceLocation(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d"));
            }
            return null;
        });
    }
}