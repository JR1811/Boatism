package net.shirojr.boatism.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.DefaultedList;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.mixin.BoatEntityInvoker;
import net.shirojr.boatism.network.BoatismS2C;
import net.shirojr.boatism.sound.BoatismSounds;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BoatEngineHandler {
    public static final int MAX_FUEL = 2000, MAX_POWER_LEVEL = 9, MAX_OVERHEAT_TICKS = 80;

    private BoatEngineEntity boatEngine;
    private final DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4);
    private float fuel;
    private int powerLevel;
    private boolean isSubmerged;
    private final boolean hasLowFuel;
    private final boolean isResting;
    private boolean isRunning;
    private int overHeatTicks;

    private BoatEngineHandler(BoatEngineEntity boatEngine, List<ItemStack> heldItems, List<ItemStack> armorItems, float fuel, int powerLevel,
                              boolean isSubmerged, boolean hasLowFuel, boolean isResting, boolean isRunning) {
        this.boatEngine = boatEngine;
        this.heldItems.addAll(heldItems);
        this.armorItems.addAll(armorItems);
        this.fuel = fuel;
        this.powerLevel = powerLevel;
        this.isSubmerged = isSubmerged;
        this.hasLowFuel = hasLowFuel;
        this.isResting = isResting;
        this.isRunning = isRunning;
        this.overHeatTicks = 0;
    }

    public static BoatEngineHandler create(BoatEngineEntity boatEngine, List<ItemStack> heldItems, List<ItemStack> armorItems) {
        BoatEngineHandler engineHandler = new BoatEngineHandler(boatEngine, heldItems, armorItems,0.0f, 0,
                false, true, false, false);
        engineHandler.initiateSoundStateChange();
        return engineHandler;
    }

    public void incrementTick() {
        if (this.boatEngine.getWorld().isClient()) return;
        if (breaksWhenSubmerged() && isSubmerged()) stopEngine();
        if (getFuel() <= 0) stopEngine();

        if (isOverheating()) stopEngine();
        if (isExperiencingHeavyLoad()) {
            this.overHeatTicks = this.overHeatTicks + 2;
        } else if (this.overHeatTicks > 0) {
            this.overHeatTicks--;
        }

        if (!engineIsRunning()) return;

        consumeFuel(0.5f);

        LoggerUtil.devLogger(String.format("Fuel: %s | OverheatTicks: %s", getFuel(), overHeatTicks));
    }

    public boolean engineCanStart() {
        if (this.fuel < 5.0f) return false;
        if (isSubmerged() && breaksWhenSubmerged()) return false;
        if (isOverheating()) return false;
        return !this.isRunning;
    }

    public float getFuel() {
        return this.fuel;
    }

    /**
     * @param fuel amount of introduced fuel
     * @return left over fuel (bigger than 0 if engine has been filled up)
     */
    @SuppressWarnings("UnusedReturnValue")
    public float fillUpFuel(float fuel) {
        if (fuel < 0) return 0;
        playSoundEvent(BoatismSounds.BOAT_ENGINE_FILL_UP);
        float newFuelValue = getFuel() + fuel;
        if (newFuelValue > MAX_FUEL) {
            this.fuel = MAX_FUEL;
            return newFuelValue - MAX_FUEL;
        }
        this.fuel = fuel;
        return fuel;
    }

    public void consumeFuel(float baseFuelAmount) {
        float consumedFuel = baseFuelAmount + additionalConsumedFuel();
        this.fuel = Math.max(getFuel() - consumedFuel, 0);
    }

    private float additionalConsumedFuel() {
        float fuelPerTick = 0;
        for (ItemStack entry : this.armorItems) {
            if (entry.getItem() instanceof BoatComponent component && component.consumedFuelPerTick() > 0.0f) {
                fuelPerTick += component.consumedFuelPerTick();
            }
        }
        for (ItemStack entry : this.heldItems) {
            if (entry.getItem() instanceof BoatComponent component && component.consumedFuelPerTick() > 0.0f) {
                fuelPerTick += component.consumedFuelPerTick();
            }
        }
        return fuelPerTick;
    }

    public int getPowerLevel() {
        return this.powerLevel;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    public int getOverheatTicks() {
        return this.overHeatTicks;
    }

    public boolean isOverheating() {
        return getOverheatTicks() > MAX_OVERHEAT_TICKS;
    }

    public boolean isExperiencingHeavyLoad() {
        if (!engineIsRunning()) return false;
        if (this.getPowerLevel() >= MAX_POWER_LEVEL - 2) {
            soundStateChange(SoundInstanceHelper.ENGINE_OVERHEATING);
            return true;
        }
        return false;
        //TODO: add heavy load, if boat goes too slow for power level
    }

    public boolean isSubmerged() {
        return this.isSubmerged;
    }

    public void setSubmerged(boolean isSubmerged) {
        if (isSubmerged == this.isSubmerged) return;
        this.isSubmerged = isSubmerged;
        soundStateChange(SoundInstanceHelper.ENGINE_RUNNING_UNDERWATER);
    }

    public boolean breaksWhenSubmerged() {
        boolean hasWaterProofedArmorStacks = this.armorItems.stream().anyMatch(stack ->
                stack.getItem() instanceof BoatComponent component && component.waterProofesEngine());
        boolean hasWaterProofedEquippedStacks = this.heldItems.stream().anyMatch(stack ->
                stack.getItem() instanceof BoatComponent component && component.waterProofesEngine());

        return !hasWaterProofedArmorStacks && !hasWaterProofedEquippedStacks;
    }

    public void startEngine() {
        if (!engineCanStart()) {
            playSoundEvent(BoatismSounds.BOAT_ENGINE_START_FAIL);
            return;
        }
        playSoundEvent(BoatismSounds.BOAT_ENGINE_START);
        this.isRunning = true;
        soundStateChange(SoundInstanceHelper.ENGINE_RUNNING);
    }

    public void stopEngine() {
        if (!engineIsRunning()) return;
        playSoundEvent(BoatismSounds.BOAT_ENGINE_STOP);
        this.powerLevel = 0;
        this.isRunning = false;
    }

    public boolean engineIsRunning() {
        return this.isRunning;
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


    public void initiateSoundStateChange() {
        List<SoundInstanceHelper> instances = new ArrayList<>();
        if (engineIsRunning()) instances.add(SoundInstanceHelper.ENGINE_RUNNING);
        if (isOverheating()) instances.add(SoundInstanceHelper.ENGINE_OVERHEATING);
        if (isSubmerged()) {
            instances.add(SoundInstanceHelper.ENGINE_RUNNING_UNDERWATER);
            instances.remove(SoundInstanceHelper.ENGINE_RUNNING);
            instances.remove(SoundInstanceHelper.ENGINE_OVERHEATING);
        }

        for (SoundInstanceHelper entry : instances) {
            soundStateChange(entry);
        }
    }

    private void soundStateChange(@NotNull SoundInstanceHelper soundInstance) {
        if (!(boatEngine.getWorld() instanceof ServerWorld serverWorld)) return;
        PlayerLookup.around(serverWorld, boatEngine.getPos(), 20).forEach(player -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(soundInstance.getIdentifier());
            buf.writeVarInt(this.boatEngine.getId());
            ServerPlayNetworking.send(player, BoatismS2C.CUSTOM_SOUND_INSTANCE_PACKET, buf);
        });
    }

    private void playSoundEvent(SoundEvent soundEvent) {
        if (this.boatEngine.getWorld().isClient()) return;
        this.boatEngine.getWorld().playSound(null, boatEngine.getBlockPos(),
                BoatismSounds.BOAT_ENGINE_STOP, SoundCategory.NEUTRAL, 1.0f, 1.0f);
    }
}
