package net.shirojr.boatism.block.custom.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.shirojr.boatism.block.BoatismBlocks;
import net.shirojr.boatism.block.BoatismFluids;
import net.shirojr.boatism.item.BoatismItems;
import net.shirojr.boatism.sound.BoatismSounds;

import java.util.Optional;

public abstract class OilFluid extends FlowableFluid {
    @Override
    public Fluid getFlowing() {
        return BoatismFluids.OIL.flowing();
    }

    @Override
    public Fluid getStill() {
        return BoatismFluids.OIL.still();
    }

    @Override
    protected boolean isInfinite(World world) {
        return BoatismFluids.OIL.isInfinite();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
        super.appendProperties(builder);
        builder.add(LEVEL);
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected int getFlowSpeed(WorldView world) {
        return 2;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return 2;
    }

    @Override
    public Item getBucketItem() {
        return BoatismItems.FUEL_BUCKET;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return direction.equals(Direction.DOWN) && !BoatismFluids.OIL.contains(state);
    }

    @Override
    public int getTickRate(WorldView world) {
        return 30;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0f;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return BoatismBlocks.OIL_FLUID_BLOCK.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
    }

    @Override
    protected void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        if (state.isStill() && random.nextInt(400) == 0) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), BoatismSounds.OIL_AMBIENT, SoundCategory.BLOCKS,
                    0.2f + random.nextFloat() * 0.2f, 0.95f + random.nextFloat() * 0.15f, false);
        } else if (!state.isStill() && !state.get(FALLING)) {
            if (random.nextInt(64) == 0) {
                world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS,
                        random.nextFloat() * 0.25f + 0.75f, random.nextFloat() + 0.5f,
                        false);
            }
        }
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(SoundEvents.ITEM_BUCKET_FILL_LAVA);
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid.equals(this.getFlowing()) || fluid.equals(this.getStill());
    }


    public static class Still extends OilFluid {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends OilFluid {
        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }
}
