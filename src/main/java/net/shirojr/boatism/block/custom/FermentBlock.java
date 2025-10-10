package net.shirojr.boatism.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.shirojr.boatism.block.entity.custom.FermentBlockEntity;
import net.shirojr.boatism.init.BoatismBlockEntities;
import org.jetbrains.annotations.Nullable;

public class FermentBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BooleanProperty.of("open_lid");
    public static final EnumProperty<Part> PART = EnumProperty.of("part", Part.class);

    public FermentBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(OPEN, false)
                .with(PART, Part.MID)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, PART);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (!state.contains(PART) || state.get(PART) != Part.MID) return null;
        return new FermentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == BoatismBlockEntities.FERMENTER
                ? (world1, pos, state1, blockEntity) -> FermentBlockEntity.tick(world1, pos, state1, (FermentBlockEntity) blockEntity)
                : null;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return createCuboidShape(1, 0, 1, 15, 16, 15);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (state.get(PART) == Part.MID) {
            world.setBlockState(pos.up(), state.with(PART, Part.TOP), Block.NOTIFY_ALL);
        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        FermentBlockEntity blockEntity = getBlockEntity(world, pos);
        if (blockEntity == null) return ActionResult.PASS;

        if (blockEntity.insertAndEmptyCanister(player, hand)) {
            return ActionResult.success(world.isClient);
        }
        if (blockEntity.extractAndFillCanister(player, hand)) {
            return ActionResult.success(world.isClient);
        }
        if (blockEntity.getInventory().canInsert(stack)) {
            blockEntity.modifyInventory((inventory, world1) -> {
                boolean wasInserted = inventory.insert(stack.copy());
                if (wasInserted && !player.isCreative()) stack.decrement(stack.getCount());
            });
            return ActionResult.success(world.isClient);
        }

        if (player.isSneaking()) {
            toggleOpenedState(world, pos);
            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    public static void toggleOpenedState(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.contains(OPEN) || !state.contains(PART)) return;
        boolean newOpen = !state.get(OPEN);
        world.setBlockState(pos, state.with(OPEN, newOpen), Block.NOTIFY_ALL);

        BlockPos offsetPos = state.get(PART) == Part.TOP ? pos.down() : pos.up();
        BlockState offsetState = world.getBlockState(offsetPos);
        if (offsetState.contains(OPEN) && offsetState.contains(PART)) {
            world.setBlockState(offsetPos, offsetState.with(OPEN, newOpen), Block.NOTIFY_ALL);
        }

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 2f, 1f);
        }
    }

    @Nullable
    public static FermentBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockPos.Mutable mutPos = pos.mutableCopy();
        BlockState state = world.getBlockState(mutPos);
        if (!state.contains(PART)) return null;
        if (state.get(PART) == Part.TOP) mutPos.move(Direction.DOWN);
        if (!(world.getBlockEntity(mutPos) instanceof FermentBlockEntity blockEntity)) return null;
        return blockEntity;
    }

    public enum Part implements StringIdentifiable {
        MID("mid"),
        TOP("top");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public String getName() {
            return this.asString();
        }

        @Nullable
        public static Part getPart(String name) {
            for (Part entry : Part.values()) {
                if (entry.getName().equals(name)) return entry;
            }
            return null;
        }
    }
}
