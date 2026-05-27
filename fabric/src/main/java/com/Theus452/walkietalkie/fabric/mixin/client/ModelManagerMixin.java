package com.Theus452.walkietalkie.fabric.mixin.client;

import com.Theus452.walkietalkie.client.model.ExtraModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin implements ExtraModelManager {
    @Override
    public BakedModel walkietalkie$getExtraModel(ResourceLocation modelId) {
        return ((FabricBakedModelManager) (Object) this).getModel(modelId);
    }
}
