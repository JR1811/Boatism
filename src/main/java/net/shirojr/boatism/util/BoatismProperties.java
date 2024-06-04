package net.shirojr.boatism.util;

import net.minecraft.state.property.IntProperty;

public class BoatismProperties {
    public static final IntProperty FLUID_HEAT;

    static {
        FLUID_HEAT = IntProperty.of("fluid_heat", 0, Bounds.FLUID_HEAT_MAX);
    }

    public static class Bounds {
        public static final int FLUID_HEAT_MAX = 10;
    }
}
