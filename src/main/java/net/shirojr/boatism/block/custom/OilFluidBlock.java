package net.shirojr.boatism.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.shirojr.boatism.fluid.BoatismFluids;
import net.shirojr.boatism.util.BoatismProperties;

public class OilFluidBlock extends FluidBlock {
    private static final IntProperty FLUID_HEAT = BoatismProperties.FLUID_HEAT;
    public OilFluidBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(LEVEL, 0).with(FLUID_HEAT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FLUID_HEAT);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public static int getHeat(World world, BlockPos pos) {
        FluidState state = world.getFluidState(pos);
        if (!BoatismFluids.OIL.contains(state)) return 0;
        return state.get(FLUID_HEAT);
    }
}
