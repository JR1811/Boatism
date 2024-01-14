package net.shirojr.boatism.util;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.EulerAngle;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.item.BoatismItems;

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

    public static List<ItemStack> readItemStacksFromNbt(NbtCompound nbt, String name) {
        List<ItemStack> stacks = new ArrayList<>();
        if (nbt.contains(name, NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbt.getList(name, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < nbtList.size(); i++) {
                stacks.add(ItemStack.fromNbt(nbtList.getCompound(i)));
            }
        }
        return stacks;
    }

    public static ItemStack getItemStackFromBoatEngineEntity(BoatEngineEntity engineEntity) {
        ItemStack engineStack = new ItemStack(BoatismItems.BASE_ENGINE);
        NbtCompound nbt = engineStack.getOrCreateNbt();

        List<ItemStack> mountedList = engineEntity.getMountedInventory().getHeldStacks().stream().toList();
        engineEntity.getHookedBoatEntityUuid().ifPresent(hookedBoatEntityUuid ->
                nbt.putUuid(NbtKeys.HOOKED_ENTITY, hookedBoatEntityUuid));
        BoatEngineNbtHelper.writeItemStacksToNbt(mountedList, NbtKeys.MOUNTED_ITEMS, nbt);
        nbt.putInt(NbtKeys.POWER_OUTPUT, engineEntity.getPowerLevel());
        nbt.putFloat(NbtKeys.OVERHEAT, engineEntity.getOverheat());
        nbt.put(NbtKeys.ROTATION, engineEntity.getArmRotation().toNbt());
        nbt.putBoolean(NbtKeys.IS_SUBMERGED, engineEntity.isSubmerged());
        nbt.putFloat(NbtKeys.FUEL, engineEntity.getFuel());
        nbt.putBoolean(NbtKeys.IS_LOCKED, engineEntity.isLocked());
        return engineStack;
    }

    public static List<ItemStack> getMountedItemsFromBoatEngineEntity(BoatEngineEntity engineEntity) {
        List<ItemStack> returnedItemStacks = new ArrayList<>();
        engineEntity.getMountedInventory().getHeldStacks().forEach(stack -> {
            if (stack.getItem() instanceof BoatEngineComponent component) {
                returnedItemStacks.add(component.getReturnedItemStack(stack));
            }
        });
        return returnedItemStacks;
    }

    @SuppressWarnings("CommentedOutCode")
    public static BoatEngineEntity getBoatEngineEntityFromItemStack(ItemStack stack, BoatEntity linkedBoat) {
        BoatEngineEntity boatEngine = new BoatEngineEntity(linkedBoat.getWorld(), linkedBoat);
        NbtCompound stackNbt = stack.getOrCreateNbt();

        // hook will be done with entity later on
        /*if (stackNbt.contains(NbtKeys.HOOKED_ENTITY)) {
            boatEngine.setHookedBoatEntity(stackNbt.getUuid("HookedEntity"));
        }*/
        if (stackNbt.contains(NbtKeys.MOUNTED_ITEMS)) {
            boatEngine.setMountedItemsFromItemStackList(BoatEngineNbtHelper.readItemStacksFromNbt(stackNbt, NbtKeys.MOUNTED_ITEMS));
        }
        boatEngine.setPowerLevel(Math.min(stackNbt.getInt(NbtKeys.POWER_OUTPUT), BoatEngineHandler.MAX_POWER_LEVEL / 2));
        boatEngine.setOverheat(stackNbt.getFloat(NbtKeys.OVERHEAT));
        boatEngine.setArmRotation(new EulerAngle(stackNbt.getList(NbtKeys.ROTATION, NbtElement.FLOAT_TYPE)));
        boatEngine.setSubmerged(stackNbt.getBoolean(NbtKeys.IS_SUBMERGED));
        boatEngine.setFuel(stackNbt.getFloat(NbtKeys.FUEL));
        boatEngine.setLocked(stackNbt.getBoolean(NbtKeys.IS_LOCKED));
        return boatEngine;
    }
}
