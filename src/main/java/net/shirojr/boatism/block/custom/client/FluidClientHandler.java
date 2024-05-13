package net.shirojr.boatism.block.custom.client;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.render.RenderLayer;
import net.shirojr.boatism.block.BoatismFluids;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.data.FlowableFluidCombination;

public class FluidClientHandler {
    static {
        registerFluidsOnClient(BoatismFluids.OIL);
    }

    private static void registerFluidsOnClient(FlowableFluidCombination fluid) {
        SimpleFluidRenderHandler handler = new SimpleFluidRenderHandler(
                fluid.getTextureLocation(FlowableFluidCombination.Type.STILL),
                fluid.getTextureLocation(FlowableFluidCombination.Type.FLOWING));
        FluidRenderHandlerRegistry.INSTANCE.register(fluid.still(), fluid.flowing(), handler);
        // no infinite and overlay handling yet

        BlockRenderLayerMap.INSTANCE.putFluid(fluid.flowing(), RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putFluid(fluid.still(), RenderLayer.getTranslucent());
    }

    public static void initialize() {
        LoggerUtil.devLogger("initialized fluids on client side");
    }
}
