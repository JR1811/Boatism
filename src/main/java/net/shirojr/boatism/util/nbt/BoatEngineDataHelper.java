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
import net.shirojr.boatism.init.BoatismDataComponents;
import net.shirojr.boatism.init.BoatismItems;
import net.shirojr.boatism.item.util.EnginePlacer;
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
        engineEntity.getHookedBoatEntityUuid().ifPresent(uuid -> engineStack.set(BoatismDataComponents.HOOKED_ENTITY, uuid));

        LinkedHashSet<BoatismCodecs.MountedInventory.Slot> mountedInventory = new LinkedHashSet<>();
        for (int i = 0; i < engineEntity.getMountedInventory().getHeldStacks().size(); i++) {
            ItemStack stack = engineEntity.getMountedInventory().getStack(i);
            if (stack.isEmpty()) continue;
            mountedInventory.add(new BoatismCodecs.MountedInventory.Slot(i, stack));
        }
        engineStack.set(BoatismDataComponents.MOUNTED_ITEMS, mountedInventory);
        engineStack.set(BoatismDataComponents.RUNNING, engineEntity.isRunning());
        engineStack.set(BoatismDataComponents.POWER_OUTPUT, engineEntity.getPowerLevel());
        engineStack.set(BoatismDataComponents.OVERHEAT, engineEntity.getOverheat());
        engineStack.set(BoatismDataComponents.ROTATION, engineEntity.getArmRotation());
        engineStack.set(BoatismDataComponents.IS_SUBMERGED, engineEntity.isSubmerged());
        engineStack.set(BoatismDataComponents.FUEL, engineEntity.getFuel());
        engineStack.set(BoatismDataComponents.LOCKED, engineEntity.isLocked());
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

    @Nullable
    public static BoatEngineEntity getBoatEngineEntity(ItemStack stack, BoatEntity linkedBoat) {
        if (!(stack.getItem() instanceof EnginePlacer enginePlacer)) return null;
        BoatEngineEntity boatEngine = enginePlacer.getEngineInstance(linkedBoat.getWorld(), linkedBoat);

        LinkedHashSet<BoatismCodecs.MountedInventory.Slot> mountedInventory = stack.get(BoatismDataComponents.MOUNTED_ITEMS);
        if (mountedInventory != null) {
            boatEngine.setMountedItemsFromItemStackList(mountedInventory);
        }
        boatEngine.setIsRunning(stack.getOrDefault(BoatismDataComponents.RUNNING, false));
        boatEngine.setPowerLevel(MathHelper.clamp(stack.getOrDefault(BoatismDataComponents.POWER_OUTPUT, 0), 0, BoatEngineHandler.MAX_POWER_LEVEL / 2));
        float overheat = (float) MathHelper.clamp(stack.getOrDefault(BoatismDataComponents.OVERHEAT, 0f), 0, boatEngine.getEngineHandler().getMaxOverHeatCapacity() * 0.8);
        boatEngine.setOverheat(overheat);
        boatEngine.setArmRotation(stack.getOrDefault(BoatismDataComponents.ROTATION, new EulerAngle(0, 0, 0)));
        boatEngine.setSubmerged(stack.getOrDefault(BoatismDataComponents.IS_SUBMERGED, false));
        boatEngine.setFuel(stack.getOrDefault(BoatismDataComponents.FUEL, 0L));
        boatEngine.setLocked(stack.getOrDefault(BoatismDataComponents.LOCKED, false));
        return boatEngine;
    }
}
