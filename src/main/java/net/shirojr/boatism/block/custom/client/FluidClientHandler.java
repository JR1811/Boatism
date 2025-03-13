package net.shirojr.boatism.block.custom.client;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockRenderView;
import net.shirojr.boatism.init.BoatismFluids;
import net.shirojr.boatism.init.BoatismProperties;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.data.FlowableFluidCombination;
import org.jetbrains.annotations.Nullable;

public class FluidClientHandler {
    static {
        registerFluidsOnClient(BoatismFluids.OIL);
    }

    private static void registerFluidsOnClient(FlowableFluidCombination fluid) {
        SimpleFluidRenderHandler handler = new SimpleFluidRenderHandler(
                fluid.getTextureLocation(FlowableFluidCombination.Type.STILL),
                fluid.getTextureLocation(FlowableFluidCombination.Type.FLOWING)){
            @Override
            public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                if (!state.contains(BoatismProperties.FLUID_HEAT) && !state.getBlockState().contains(BoatismProperties.FLUID_HEAT))
                    return super.getFluidColor(view, pos, state);
                float heat = state.get(BoatismProperties.FLUID_HEAT) / (float) BoatismProperties.Bounds.FLUID_HEAT_MAX;
                return ColorHelper.Argb.lerp(heat, 0x453024, 0xc73522);
            }
        };
        FluidRenderHandlerRegistry.INSTANCE.register(fluid.still(), fluid.flowing(), handler);
        // no infinite and overlay handling yet

        BlockRenderLayerMap.INSTANCE.putFluid(fluid.flowing(), RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putFluid(fluid.still(), RenderLayer.getTranslucent());
    }

    public static void initialize() {
        LoggerUtil.devLogger("initialized fluids on client side");
    }
}
