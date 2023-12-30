package net.shirojr.boatism.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class BoatEngineNbtHelper {

    public static void writeItemStacksToNbt(List<ItemStack> stacks, String name, NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (ItemStack itemStack : stacks) {
            NbtCompound nbtCompound = new NbtCompound();
            if (!itemStack.isEmpty()) {
                itemStack.writeNbt(nbtCompound);
            }
            nbtList.add(nbtCompound);
        }
        nbt.put(name, nbtList);
    }

    public static DefaultedList<ItemStack> readItemStacksFromNbt(NbtCompound nbt, String name, int size) {
        NbtList nbtList;
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size);
        if (nbt.contains(name, NbtElement.LIST_TYPE)) {
            nbtList = nbt.getList(name, NbtElement.COMPOUND_TYPE);
            stacks = DefaultedList.ofSize(nbtList.size());
            for (int i = 0; i < size; i++) {
                stacks.set(i, ItemStack.fromNbt(nbtList.getCompound(i)));
                //this.armorItems.set(i, ItemStack.fromNbt(nbtList.getCompound(i)));
            }
        }
        return stacks;
    }
}
