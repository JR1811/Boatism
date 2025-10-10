package net.shirojr.boatism.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class InventoryUtils {
    public static DefaultedList<ItemStack> getAllStacks(SimpleInventory inventory) {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.size(); i++) {
            stacks.set(i, inventory.getStack(i));
        }
        return stacks;
    }
}
