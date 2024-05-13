package net.shirojr.boatism.util.data;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import org.jetbrains.annotations.Nullable;

public record FlowableFluidCombination(String base, FlowableFluid flowing, FlowableFluid still, @Nullable FlowableFluid infinite) {
    public boolean isInfinite() {
        return infinite != null;
    }

    public String getName(Type type) {
        return type.getFluidName(this.base);
    }

    public Identifier getTextureLocation(Type type) {
        return type.getPath(this.base);
    }

    public Identifier getOverlayTextureLocation(String fluidId) {
        return new Identifier(Boatism.MODID, "misc/" + fluidId + "_overlay");
    }

    public boolean contains(FlowableFluid fluid) {
        if (this.flowing().equals(fluid)) return true;
        if (this.still().equals(fluid)) return true;
        return this.infinite() != null && this.infinite().equals(fluid);
    }

    public boolean contains(FluidState state) {
        if (!(state.getFluid() instanceof FlowableFluid flowableFluid)) return false;
        return contains(flowableFluid);
    }

    public enum Type {
        FLOWING("_flowing"),
        STILL("_still"),
        INFINITE("_infinite");

        private final String fluidIdAddition;

        Type(String addition) {
            this.fluidIdAddition = addition;
        }

        public String getFluidName(String baseId) {
            return baseId + this.fluidIdAddition;
        }

        public Identifier getPath(String fluidBaseId) {
            return new Identifier(Boatism.MODID, "block/" + getFluidName(fluidBaseId));
        }
    }
}
