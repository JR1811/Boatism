package net.shirojr.boatism.util.nbt;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismItems;
import net.shirojr.boatism.item.util.EnginePlacer;
import net.shirojr.boatism.util.InventoryUtils;
import net.shirojr.boatism.util.data.codec.BoatismCodecs;
import net.shirojr.boatism.util.handler.BoatEngineHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class BoatEngineDataHelper {

    public static void writeItemStacksToNbt(DefaultedList<ItemStack> stacks, String name, NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) continue;
            BoatismCodecs.MountedInventory.Slot slot = new BoatismCodecs.MountedInventory.Slot(i, stack);
            BoatismCodecs.MountedInventory.Slot.CODEC.encodeStart(NbtOps.INSTANCE, slot).result().ifPresent(nbtList::add);
        }
        if (nbtList.isEmpty()) return;
        nbt.put(name, nbtList);
    }

    public static LinkedHashSet<BoatismCodecs.MountedInventory.Slot> readItemStacksFromNbt(NbtCompound nbt, String name) {
        LinkedHashSet<BoatismCodecs.MountedInventory.Slot> stacks = new LinkedHashSet<>();
        if (nbt.contains(name, NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbt.getList(name, NbtElement.COMPOUND_TYPE);
            nbtList.forEach(nbtElement -> BoatismCodecs.MountedInventory.Slot.CODEC.parse(NbtOps.INSTANCE, nbtElement).result().ifPresent(stacks::add));
        }
        return stacks;
    }

    public static ItemStack getItemStackFromBoatEngineEntity(BoatEngineEntity engineEntity) {
        ItemStack engineStack = new ItemStack(BoatismItems.BASE_ENGINE);
        NbtCompound nbt = engineStack.getOrCreateNbt();
        DefaultedList<ItemStack> mountedList = DefaultedList.of();
        mountedList.addAll(InventoryUtils.getAllStacks(engineEntity.getMountedInventory()));
        engineEntity.getHookedBoatEntityUuid().ifPresent(hookedBoatEntityUuid -> nbt.putUuid(NbtKeys.HOOKED_ENTITY, hookedBoatEntityUuid));
        BoatEngineDataHelper.writeItemStacksToNbt(mountedList, NbtKeys.MOUNTED_ITEMS, nbt);
        nbt.putBoolean(NbtKeys.IS_RUNNING, engineEntity.isRunning());
        nbt.putInt(NbtKeys.POWER_OUTPUT, engineEntity.getPowerLevel());
        nbt.putFloat(NbtKeys.OVERHEAT, engineEntity.getOverheat());
        nbt.put(NbtKeys.ROTATION, engineEntity.getArmRotation().toNbt());
        nbt.putBoolean(NbtKeys.IS_SUBMERGED, engineEntity.isSubmerged());
        nbt.putFloat(NbtKeys.FUEL, engineEntity.getFuel());
        nbt.putBoolean(NbtKeys.IS_LOCKED, engineEntity.isLocked());
        return engineStack;
    }

    public static ItemStack getItemStackFromAirBoatEngineEntity(BoatEngineEntity engineEntity) {
        ItemStack engineStack = new ItemStack(BoatismItems.AIR_BOAT_ENGINE);
        NbtCompound nbt = engineStack.getOrCreateNbt();
        DefaultedList<ItemStack> mountedList = DefaultedList.of();
        mountedList.addAll(InventoryUtils.getAllStacks(engineEntity.getMountedInventory()));
        engineEntity.getHookedBoatEntityUuid().ifPresent(uuid -> nbt.putUuid(NbtKeys.HOOKED_ENTITY, uuid));
        BoatEngineDataHelper.writeItemStacksToNbt(mountedList, NbtKeys.MOUNTED_ITEMS, nbt);
        nbt.putBoolean(NbtKeys.IS_RUNNING, engineEntity.isRunning());
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
        InventoryUtils.getAllStacks(engineEntity.getMountedInventory()).forEach(stack -> {
            if (stack.getItem() instanceof BoatEngineComponent component) {
                returnedItemStacks.add(component.getReturnedItemStack(stack));
            }
        });
        return returnedItemStacks;
    }

    @Nullable
    public static BoatEngineEntity getBoatEngineEntity(ItemStack stack, BoatEntity linkedBoat) {
        if (!(stack.getItem() instanceof EnginePlacer enginePlacer)) return null;

        BoatEngineEntity boatEngine = enginePlacer.getEngineInstance(linkedBoat.getWorld(), linkedBoat);
        NbtCompound nbt = stack.getOrCreateNbt();

        LinkedHashSet<BoatismCodecs.MountedInventory.Slot> mountedInventory =
                BoatEngineDataHelper.readItemStacksFromNbt(nbt, NbtKeys.MOUNTED_ITEMS);
        boatEngine.setMountedItemsFromItemStackList(mountedInventory);

        boatEngine.setIsRunning(nbt.getBoolean(NbtKeys.IS_RUNNING));
        boatEngine.setPowerLevel(MathHelper.clamp(nbt.getInt(NbtKeys.POWER_OUTPUT), 0, BoatEngineHandler.MAX_POWER_LEVEL / 2));
        boatEngine.setOverheat(MathHelper.clamp(nbt.getFloat(NbtKeys.OVERHEAT), 0f, boatEngine.getEngineHandler().getMaxOverHeatCapacity() * 0.8f));

        if (nbt.contains(NbtKeys.ROTATION, NbtElement.LIST_TYPE)) {
            NbtList rotationList = nbt.getList(NbtKeys.ROTATION, NbtElement.FLOAT_TYPE);
            float pitch = rotationList.size() > 0 ? rotationList.getFloat(0) : 0.0F;
            float yaw   = rotationList.size() > 1 ? rotationList.getFloat(1) : 0.0F;
            float roll  = rotationList.size() > 2 ? rotationList.getFloat(2) : 0.0F;
            boatEngine.setArmRotation(new EulerAngle(pitch, yaw, roll));
        } else {
            boatEngine.setArmRotation(new EulerAngle(0, 0, 0));
        }

        boatEngine.setSubmerged(nbt.getBoolean(NbtKeys.IS_SUBMERGED));
        boatEngine.setFuel( nbt.getLong(NbtKeys.FUEL)); // если FUEL был float в NBT
        boatEngine.setLocked(nbt.getBoolean(NbtKeys.IS_LOCKED));

        return boatEngine;
    }

}
