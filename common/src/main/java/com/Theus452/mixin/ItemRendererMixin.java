package com.Theus452.mixin;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.client.model.ExtraModelManager;
import com.Theus452.walkietalkie.item.ModItems;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    private static final ResourceLocation WALKIE_TALKIE_3D_MODEL_ID = ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "item/walkie_talkie3d");
    private static final ModelResourceLocation WALKIE_TALKIE_3D_MODEL_LOCATION = new ModelResourceLocation(WALKIE_TALKIE_3D_MODEL_ID, "standalone");

    @Shadow @Final
    private ItemModelShaper itemModelShaper;

    @ModifyVariable(method = "render", at = @At(value = "HEAD"), argsOnly = true)
    public BakedModel useWalkieTalkie3DModel(BakedModel originalModel, ItemStack stack, ItemDisplayContext displayContext) {
        if (stack.is(ModItems.WALKIETALKIE.get()) && displayContext != ItemDisplayContext.GUI) {
            ModelManager modelManager = this.itemModelShaper.getModelManager();
            if (modelManager instanceof ExtraModelManager extraModelManager) {
                BakedModel model = extraModelManager.walkietalkie$getExtraModel(WALKIE_TALKIE_3D_MODEL_ID);
                if (model != null) {
                    return model;
                }
            }
            return modelManager.getModel(WALKIE_TALKIE_3D_MODEL_LOCATION);
        }

        return originalModel;
    }
}
