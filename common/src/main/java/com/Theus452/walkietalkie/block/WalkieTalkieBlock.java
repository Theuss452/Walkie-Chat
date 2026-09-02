package com.Theus452.walkietalkie.block;

import com.Theus452.walkietalkie.item.ModItems;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import com.Theus452.walkietalkie.sound.ModSounds;
import com.Theus452.walkietalkie.util.ConnectionManager;
import com.Theus452.walkietalkie.util.WalkieSecurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class WalkieTalkieBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WALL = BooleanProperty.create("wall");

    private static final VoxelShape SHAPE_NORTH = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    private static final VoxelShape SHAPE_EAST = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    private static final VoxelShape SHAPE_WEST = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);

    private static final VoxelShape WALL_SHAPE_NORTH = Block.box(4.0, 3.0, 9.0, 12.0, 13.0, 16.0);
    private static final VoxelShape WALL_SHAPE_SOUTH = Block.box(4.0, 3.0, 0.0, 12.0, 13.0, 7.0);
    private static final VoxelShape WALL_SHAPE_EAST = Block.box(0.0, 3.0, 4.0, 7.0, 13.0, 12.0);
    private static final VoxelShape WALL_SHAPE_WEST = Block.box(9.0, 3.0, 4.0, 16.0, 13.0, 12.0);

    public WalkieTalkieBlock() {
        super(BlockBehaviour.Properties.of(Material.HEAVY_METAL)
                .strength(0.5F)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WALL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WALL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (clickedFace.getAxis().isHorizontal()) {
            Direction wallDirection = clickedFace.getOpposite();
            BlockPos wallPos = pos.relative(wallDirection);
            if (level.getBlockState(wallPos).isFaceSturdy(level, wallPos, clickedFace)) {
                return this.defaultBlockState()
                        .setValue(FACING, wallDirection)
                        .setValue(WALL, true);
            }
        }

        BlockPos belowPos = pos.below();
        if (level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection())
                    .setValue(WALL, false);
        }

        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(WALL)) {
            Direction wallDirection = state.getValue(FACING);
            BlockPos wallPos = pos.relative(wallDirection);
            return level.getBlockState(wallPos).isFaceSturdy(level, wallPos, wallDirection.getOpposite());
        }
        BlockPos belowPos = pos.below();
        return level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(WALL)) {
            return switch (state.getValue(FACING)) {
                case SOUTH -> WALL_SHAPE_SOUTH;
                case EAST -> WALL_SHAPE_EAST;
                case WEST -> WALL_SHAPE_WEST;
                default -> WALL_SHAPE_NORTH;
            };
        }
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WalkieTalkieBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlocks.WALKIE_TALKIE_BLOCK_ENTITY.get(), WalkieTalkieBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof WalkieTalkieBlockEntity walkieBE)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                if (player instanceof ServerPlayer sp && !WalkieSecurity.canControlWalkieBlock(sp, walkieBE)) {
                    player.displayClientMessage(Component.translatable("message.walkietalkie.block.not_owner"), true);
                    return InteractionResult.CONSUME;
                }

                ItemStack dropStack = new ItemStack(ModItems.WALKIETALKIE.get());
                applyBlockDataToStack(dropStack, walkieBE);

                level.removeBlock(pos, false);
                if (ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL != null) {
                    level.playSound(null, pos, ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }

                if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, dropStack);
                } else if (!player.getInventory().add(dropStack)) {
                    player.drop(dropStack, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (level.isClientSide()) {
            ClientOnly.openBlockScreen(pos, walkieBE.getFrequency());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && placer instanceof ServerPlayer player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WalkieTalkieBlockEntity walkieBE) {
                applyStackDataToBlock(walkieBE, stack, player);
                String freq = walkieBE.getFrequency();
                if (!freq.isEmpty()) {
                    ConnectionManager.refreshPlayer(player);
                }
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof WalkieTalkieBlockEntity walkieBE) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof WalkieTalkieItem) {
                    applyBlockDataToStack(drop, walkieBE);
                }
            }
        }
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof WalkieTalkieBlockEntity walkieBE) {
            applyBlockDataToStack(stack, walkieBE);
        }
        return stack;
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    public static void applyBlockDataToStack(ItemStack stack, WalkieTalkieBlockEntity block) {
        if (stack == null || stack.isEmpty() || block == null) return;
        if (!(stack.getItem() instanceof WalkieTalkieItem)) return;
        WalkieTalkieItem.setFrequency(stack, block.getFrequency());
        String name = block.getChannelName();
        WalkieTalkieItem.setChannelName(stack, name);
        WalkieTalkieItem.setBlockOwner(stack, block.ownerUUID);
        WalkieTalkieItem.setBlockRelayEnabled(stack, block.isRelayEnabled());
    }

    public static void applyStackDataToBlock(WalkieTalkieBlockEntity block, ItemStack stack, Player placer) {
        if (block == null || stack == null) return;
        String freq = WalkieTalkieItem.getFrequency(stack);
        if (!freq.isEmpty()) {
            block.setFrequency(freq);
        }
        String name = WalkieTalkieItem.getChannelName(stack);
        if (!name.isEmpty()) {
            block.setChannelName(name);
        }
        if (placer != null) {
            block.setOwnerUUID(placer.getUUID());
        } else {
            UUID owner = WalkieTalkieItem.getBlockOwner(stack);
            if (owner != null) {
                block.setOwnerUUID(owner);
            }
        }
        block.setRelayEnabled(WalkieTalkieItem.getBlockRelayEnabled(stack));
    }

    private static class ClientOnly {
        private static void openBlockScreen(BlockPos pos, String frequency) {
            net.minecraft.client.Minecraft.getInstance().setScreen(
                new com.Theus452.walkietalkie.client.WalkieTalkieBlockScreen(pos, frequency)
            );
        }
    }
}
