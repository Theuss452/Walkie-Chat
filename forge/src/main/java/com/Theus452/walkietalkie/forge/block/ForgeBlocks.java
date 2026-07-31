package com.Theus452.walkietalkie.forge.block;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.block.ModBlocks;
import com.Theus452.walkietalkie.block.WalkieTalkieBlock;
import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgeBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WalkieTalkieMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WalkieTalkieMod.MOD_ID);

    public static final RegistryObject<Block> WALKIE_TALKIE_BLOCK = BLOCKS.register("walkie_talkie_block", WalkieTalkieBlock::new);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<WalkieTalkieBlockEntity>> WALKIE_TALKIE_BLOCK_ENTITY = BLOCK_ENTITIES.register("walkie_talkie_block_entity", () -> BlockEntityType.Builder.of(WalkieTalkieBlockEntity::new, WALKIE_TALKIE_BLOCK.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.WALKIE_TALKIE_BLOCK = WALKIE_TALKIE_BLOCK;
        ModBlocks.WALKIE_TALKIE_BLOCK_ENTITY = WALKIE_TALKIE_BLOCK_ENTITY;
    }
}
