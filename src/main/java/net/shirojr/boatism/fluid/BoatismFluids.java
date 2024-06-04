package net.shirojr.boatism.fluid;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.fluid.custom.OilFluid;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.data.FlowableFluidCombination;

public class BoatismFluids {
    public static FlowableFluidCombination OIL = registerFlowableFluid(new FlowableFluidCombination("oil", new OilFluid.Flowing(), new OilFluid.Still(), null));


    @SuppressWarnings("SameParameterValue")
    private static FlowableFluidCombination registerFlowableFluid(FlowableFluidCombination fluid) {
        Registry.register(Registries.FLUID, new Identifier(Boatism.MODID, fluid.getName(FlowableFluidCombination.Type.STILL)), fluid.still());
        Registry.register(Registries.FLUID, new Identifier(Boatism.MODID, fluid.getName(FlowableFluidCombination.Type.FLOWING)), fluid.flowing());
        if (fluid.isInfinite()) {
            Registry.register(Registries.FLUID, new Identifier(Boatism.MODID, fluid.getName(FlowableFluidCombination.Type.INFINITE)), fluid.infinite());
        }
        return fluid;
    }

    public static void initialize() {
        LoggerUtil.devLogger("initialized fluids");
    }
}
