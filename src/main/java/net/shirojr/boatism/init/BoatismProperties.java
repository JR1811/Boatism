package net.shirojr.boatism.init;

import net.minecraft.state.property.IntProperty;

public interface BoatismProperties {
    IntProperty FLUID_HEAT = IntProperty.of("fluid_heat", 0, Bounds.FLUID_HEAT_MAX);

    class Bounds {
        public static final int FLUID_HEAT_MAX = 10;
    }
}
