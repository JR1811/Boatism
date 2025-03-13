package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.util.math.ColorHelper;
import net.shirojr.boatism.util.LoggerUtil;

public class BoatismColorProviders {
    @SuppressWarnings("SameParameterValue")
    private static void registerColorProviderForHeat(int baseColor, Block block) {
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            LoggerUtil.devLogger("registering block color");
            if (!state.contains(BoatismProperties.FLUID_HEAT)) return baseColor;
            float heat = state.get(BoatismProperties.FLUID_HEAT) / (float) BoatismProperties.Bounds.FLUID_HEAT_MAX;
            return ColorHelper.Argb.lerp(heat, baseColor, 0x000000);
        }, block);
    }

    public static void initialize() {
        registerColorProviderForHeat(0xFFFFFF, BoatismBlocks.OIL_FLUID_BLOCK);
        LoggerUtil.devLogger("initialized color providers");
    }
}
