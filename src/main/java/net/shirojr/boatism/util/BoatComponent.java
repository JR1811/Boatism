package net.shirojr.boatism.util;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface BoatComponent {
    default List<ItemStack> getConflictingParts() {
        return List.of();
    }
    default float getThrust() {
        return 0.0f;
    }

    default float getAdditionalArmor() {
        return 0.0f;
    }
}
