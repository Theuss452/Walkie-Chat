package com.Theus452.walkietalkie.fabric.block;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.block.ModBlocks;
import com.Theus452.walkietalkie.block.WalkieTalkieBlock;
import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricBlocks {
    public static void register() {
        Block walkieBlock = new WalkieTalkieBlock();
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "walkie_talkie_block"), walkieBlock);
        ModBlocks.WALKIE_TALKIE_BLOCK = () -> walkieBlock;

        BlockEntityType<WalkieTalkieBlockEntity> beType = FabricBlockEntityTypeBuilder.create(WalkieTalkieBlockEntity::new, walkieBlock).build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(WalkieTalkieMod.MOD_ID, "walkie_talkie_block_entity"), beType);
        ModBlocks.WALKIE_TALKIE_BLOCK_ENTITY = () -> beType;
    }
}
