package net.shirojr.boatism.util;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.EulerAngle;
import net.shirojr.boatism.mixin.BoatEntityInvoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoatEngineHandler {
    public static final int MAX_FUEL = 5000, MAX_POWER_LEVEL = 9;

    private final DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4);
    private final EulerAngle armRotation;
    private float fuel;
    private final int powerLevel;
    private final boolean isSubmerged, hasLowFuel, isResting;
    private int tick;

    private BoatEngineHandler(List<ItemStack> heldItems, List<ItemStack> armorItems,
                             EulerAngle armRotation, float fuel, int powerLevel, boolean isSubmerged,
                             boolean hasLowFuel, boolean isResting) {
        this.heldItems.addAll(heldItems);
        this.armorItems.addAll(armorItems);
        this.armRotation = armRotation;
        this.fuel = fuel;
        this.powerLevel = powerLevel;
        this.isSubmerged = isSubmerged;
        this.hasLowFuel = hasLowFuel;
        this.isResting = isResting;
        this.tick = 0;
    }

    public static BoatEngineHandler create(List<ItemStack> heldItems, List<ItemStack> armorItems) {
        BoatEngineHandler handler = new BoatEngineHandler(heldItems, armorItems, new EulerAngle(0.0f, 2.0f, 0.0f),
                0.0f, 0, false, true, false);

        return handler;
    }

    public float getFuel() {
        return this.fuel;
    }
    public void setFuel(float fuel) {
        this.fuel = fuel;
    }

    public void incrementTick() {
        this.tick++;
    }

    public float calculateMaxThrust() {
        return this.calculateMaxThrust(null);
    }

    public float calculateMaxThrust(BoatEntity hookedBoatEntity) {
        if (hookedBoatEntity == null) return 0.0f;
        List<ItemStack> boatComponentStacks = new ArrayList<>();
        int passengerCount = hookedBoatEntity.getPassengerList().size();
        int maxPassenger = ((BoatEntityInvoker) hookedBoatEntity).invokeGetMaxPassenger();
        int thrust = 0;

        this.armorItems.forEach(stack -> {
            if (stack.getItem() instanceof BoatComponent component && component.getThrust() > 0.0f) {
                boatComponentStacks.add(stack);
            }
        });
        this.heldItems.forEach(stack -> {
            if (stack.getItem() instanceof BoatComponent component && component.getThrust() > 0.0f) {
                boatComponentStacks.add(stack);
            }
        });
        for (ItemStack thrustModifierStack : boatComponentStacks) {
            thrust += ((BoatComponent) thrustModifierStack.getItem()).getThrust();
        }
        return thrust * ((maxPassenger - passengerCount) * 0.1f); //TODO: implement better balancing
    }

    public float calculateConsumedFuelPerTick() {
        float fuelPerTick = 0;
        for (ItemStack entry : this.armorItems) {
            if (entry.getItem() instanceof BoatComponent component && component.consumedFuelPerTick() > 0.0f) {
                fuelPerTick += component.consumedFuelPerTick();
            }
        }
        return fuelPerTick;
    }

    public boolean canEquipPart(ItemStack stack) {
        if (!(stack.getItem() instanceof BoatComponent)) return false;
        List<ItemStack> flaggedParts = new ArrayList<>();
        for (ItemStack entry : this.armorItems) {
            if (entry.getItem() instanceof BoatComponent component) {
                flaggedParts.addAll(component.getConflictingParts());
            }
        }
        for (ItemStack entry : this.heldItems) {
            if (entry.getItem() instanceof BoatComponent component) {
                flaggedParts.addAll(component.getConflictingParts());
            }
        }
        return flaggedParts.contains(stack);
    }
}
