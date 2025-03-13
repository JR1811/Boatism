package net.shirojr.boatism.util.storage;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.util.Identifier;

public interface FluidContainer {
    boolean containsFluid();

    long getCapacity();

    Identifier getStorageIdentifier();

    default ItemApiLookup<FluidContainer, Void> getFluidContainer() {
        return ItemApiLookup.get(getStorageIdentifier(), FluidContainer.class, Void.class);
    }
}
