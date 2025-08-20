package com.theus452.mixin;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.item.ModItems;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow @Final
    private ItemModelShaper itemModelShaper;

    @ModifyVariable(method = "render", at = @At(value = "HEAD"), argsOnly = true)
    public BakedModel useWalkieTalkie3DModel(BakedModel originalModel, ItemStack stack, ItemTransforms.TransformType transformType) {
        if (stack.is(ModItems.WALKIETALKIE.get()) && transformType != ItemTransforms.TransformType.GUI) {
            return this.itemModelShaper.getModelManager().getModel(
                    new ModelResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_talkie3d", "inventory")
            );
        }

        return originalModel;
    }
}