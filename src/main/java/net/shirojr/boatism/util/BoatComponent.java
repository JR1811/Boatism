package net.shirojr.boatism.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface BoatComponent {
    default List<Item> getConflictingParts() {
        return List.of();
    }
    default float addedThrust() {
        return 0.0f;
    }
    default float addedConsumedFuel() {
        return 0.0f;
    }
    default float addedFuelCapacity() {
        return 0.0f;
    }
    default float getAdditionalArmor() {
        return 0.0f;
    }
    default boolean waterProofesEngine() {
        return false;
    }
}
