package com.Theus452.walkietalkie.neoforge.block;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.block.ModBlocks;
import com.Theus452.walkietalkie.block.WalkieTalkieBlock;
import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(WalkieTalkieMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WalkieTalkieMod.MOD_ID);

    public static final DeferredBlock<WalkieTalkieBlock> WALKIE_TALKIE_BLOCK = BLOCKS.register("walkie_talkie_block", () -> new WalkieTalkieBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WalkieTalkieBlockEntity>> WALKIE_TALKIE_BLOCK_ENTITY = BLOCK_ENTITIES.register("walkie_talkie_block_entity", () -> BlockEntityType.Builder.of(WalkieTalkieBlockEntity::new, WALKIE_TALKIE_BLOCK.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.WALKIE_TALKIE_BLOCK = WALKIE_TALKIE_BLOCK;
        ModBlocks.WALKIE_TALKIE_BLOCK_ENTITY = WALKIE_TALKIE_BLOCK_ENTITY;
    }
}
