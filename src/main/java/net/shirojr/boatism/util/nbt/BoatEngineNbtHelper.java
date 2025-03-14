package net.shirojr.boatism.util.nbt;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismDataComponents;
import net.shirojr.boatism.init.BoatismItems;
import net.shirojr.boatism.util.handler.BoatEngineHandler;

import java.util.ArrayList;
import java.util.List;

public class BoatEngineNbtHelper {

    public static void writeItemStacksToNbt(List<ItemStack> stacks, String name, NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (ItemStack itemStack : stacks) {
            if (itemStack.isEmpty()) continue;
            ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, itemStack).result().ifPresent(nbtList::add);
        }
        if (nbtList.isEmpty()) return;
        nbt.put(name, nbtList);
    }

    public static List<ItemStack> readItemStacksFromNbt(NbtCompound nbt, String name) {
        List<ItemStack> stacks = new ArrayList<>();
        if (nbt.contains(name, NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbt.getList(name, NbtElement.COMPOUND_TYPE);
            nbtList.forEach(nbtElement -> ItemStack.CODEC.parse(NbtOps.INSTANCE, nbtElement).result().ifPresent(stacks::add));
        }
        return stacks;
    }

    public static ItemStack getItemStackFromBoatEngineEntity(BoatEngineEntity engineEntity) {
        ItemStack engineStack = new ItemStack(BoatismItems.BASE_ENGINE);
        engineEntity.getHookedBoatEntityUuid().ifPresent(uuid -> engineStack.set(BoatismDataComponents.HOOKED_ENTITY, uuid));
        engineStack.set(BoatismDataComponents.MOUNTED_ITEMS, engineEntity.getMountedInventory().getHeldStacks());
        engineStack.set(BoatismDataComponents.MOUNTED_ITEMS, engineEntity.getMountedInventory().getHeldStacks());
        engineStack.set(BoatismDataComponents.IS_RUNNING, engineEntity.isRunning());
        engineStack.set(BoatismDataComponents.POWER_OUTPUT, engineEntity.getPowerLevel());
        engineStack.set(BoatismDataComponents.OVERHEAT, engineEntity.getOverheat());
        engineStack.set(BoatismDataComponents.ROTATION, engineEntity.getArmRotation());
        engineStack.set(BoatismDataComponents.IS_SUBMERGED, engineEntity.isSubmerged());
        engineStack.set(BoatismDataComponents.FUEL, engineEntity.getFuel());
        engineStack.set(BoatismDataComponents.IS_LOCKED, engineEntity.isLocked());
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

    public static BoatEngineEntity getBoatEngineEntityFromItemStack(ItemStack stack, BoatEntity linkedBoat) {
        BoatEngineEntity boatEngine = new BoatEngineEntity(linkedBoat.getWorld(), linkedBoat);
        List<ItemStack> mountedStacks = stack.get(BoatismDataComponents.MOUNTED_ITEMS);
        if (mountedStacks != null) {
            boatEngine.setMountedItemsFromItemStackList(mountedStacks);
        }
        boatEngine.setIsRunning(stack.getOrDefault(BoatismDataComponents.IS_RUNNING, false));
        boatEngine.setPowerLevel(MathHelper.clamp(stack.getOrDefault(BoatismDataComponents.POWER_OUTPUT, 0), 0, BoatEngineHandler.MAX_POWER_LEVEL / 2));
        float overheat = (float) MathHelper.clamp(stack.getOrDefault(BoatismDataComponents.OVERHEAT, 0f), 0, boatEngine.getEngineHandler().getMaxOverHeatCapacity() * 0.8);
        boatEngine.setOverheat(overheat);
        boatEngine.setArmRotation(stack.getOrDefault(BoatismDataComponents.ROTATION, new EulerAngle(0, 0, 0)));
        boatEngine.setSubmerged(stack.getOrDefault(BoatismDataComponents.IS_SUBMERGED, false));
        boatEngine.setFuel(stack.getOrDefault(BoatismDataComponents.FUEL, 0L));
        boatEngine.setLocked(stack.getOrDefault(BoatismDataComponents.IS_LOCKED, false));
        return boatEngine;
    }
}
