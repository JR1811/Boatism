package net.shirojr.boatism.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.mixin.BoatEntityInvoker;
import net.shirojr.boatism.network.BoatismS2C;
import net.shirojr.boatism.sound.BoatismSounds;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BoatEngineHandler {
    public static final int MAX_POWER_LEVEL = 9;
    public static final int MAX_BASE_FUEL = Boatism.CONFIG.maxFuel;
    public static final int MAX_OVERHEAT = Boatism.CONFIG.maxOverheat;

    private final BoatEngineEntity boatEngine;
    private final DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4);

    private boolean canPlayOverheat = true;

    private BoatEngineHandler(BoatEngineEntity boatEngine, List<ItemStack> heldItems, List<ItemStack> armorItems) {
        this.boatEngine = boatEngine;
        this.heldItems.addAll(heldItems);
        this.armorItems.addAll(armorItems);
    }

    public static BoatEngineHandler create(BoatEngineEntity boatEngine, List<ItemStack> heldItems, List<ItemStack> armorItems) {
        BoatEngineHandler engineHandler = new BoatEngineHandler(boatEngine, heldItems, armorItems);
        engineHandler.initiateSoundStateChange();
        return engineHandler;
    }

    public void incrementTick() {
        if (this.boatEngine.getWorld().isClient()) return;
        if (breaksWhenSubmerged() && isSubmerged()) stopEngine();
        if (getFuel() <= 0) stopEngine();
        if (isOverheating()) stopEngine();

        if (isExperiencingHeavyLoad()) {
            setOverheat(getOverheat() + 2);
        } else if (getOverheat() > 0) {
            setOverheat(getOverheat() - 1);
        }
        if (!canPlayOverheat && getOverheat() <= 0) {
            canPlayOverheat = true;
        }
        if (isHeatingUp()) {
            if (canPlayOverheat) {
                soundStateChange(SoundInstanceIdentifier.ENGINE_OVERHEATING);
                canPlayOverheat = false;
            }
        }
        if (isOverheating()) {
            this.boatEngine.onOverheated();
            return;
        }
        if (!engineIsRunning()) return;
        consumeFuel(1.0f);

        // LoggerUtil.devLogger(String.format("Fuel: %s | OverheatTicks: %s", getFuel(), getOverheat()));
    }

    public void startEngine() {
        if (!engineCanStart()) {
            playSoundEvent(BoatismSounds.BOAT_ENGINE_START_FAIL);
            return;
        }
        playSoundEvent(BoatismSounds.BOAT_ENGINE_START);
        this.boatEngine.setIsRunning(true);
        soundStateChange(SoundInstanceIdentifier.ENGINE_RUNNING);
    }

    public void stopEngine() {
        if (!engineIsRunning()) return;
        playSoundEvent(BoatismSounds.BOAT_ENGINE_STOP, 0.7f, 1.0f);
        setPowerLevel(0);
        this.boatEngine.setIsRunning(false);
    }

    public boolean engineIsRunning() {
        return this.boatEngine.isRunning();
    }

    public boolean engineCanStart() {
        if (getFuel() < 5.0f) return false;
        if (isSubmerged() && breaksWhenSubmerged()) return false;
        if (isOverheating()) return false;
        if (this.boatEngine.isLocked()) return false;
        return !this.boatEngine.isRunning();
    }

    public float getFuel() {
        return this.boatEngine.getFuel();
    }

    public void setFuel(float fuel) {
        this.boatEngine.setFuel(fuel);
    }

    public float getMaxFuelCapacity() {
        float maxCapacity = MAX_BASE_FUEL;

        for (ItemStack stack : armorItems) {
            if (!(stack.getItem() instanceof BoatComponent component)) continue;
            maxCapacity += component.addedFuelCapacity();
        }
        for (ItemStack stack : heldItems) {
            if (!(stack.getItem() instanceof BoatComponent component)) continue;
            maxCapacity += component.addedFuelCapacity();
        }
        return maxCapacity;
    }

    /**
     * @param fuel amount of introduced fuel
     * @return left over fuel (bigger than 0 if engine has been filled up)
     */
    @SuppressWarnings("UnusedReturnValue")
    public float fillUpFuel(float fuel) {
        float newFuelValue = getFuel() + fuel;
        if (fuel <= 0) return 0;
        if (newFuelValue == MAX_BASE_FUEL + fuel) return fuel;
        playSoundEvent(BoatismSounds.BOAT_ENGINE_FILL_UP);
        if (newFuelValue > MAX_BASE_FUEL) {
            setFuel(MAX_BASE_FUEL);
            return newFuelValue - MAX_BASE_FUEL;
        }
        setFuel(fuel);
        return fuel;
    }

    public void consumeFuel(float baseFuelConsumption) {
        float consumedFuel = baseFuelConsumption + additionalConsumedFuel();
        setFuel(Math.max(getFuel() - consumedFuel, 0));
    }

    private float additionalConsumedFuel() {
        float fuelPerTick = 0;
        for (ItemStack entry : this.armorItems) {
            if (entry.getItem() instanceof BoatComponent component && component.addedConsumedFuel() > 0.0f) {
                fuelPerTick += component.addedConsumedFuel();
            }
        }
        for (ItemStack entry : this.heldItems) {
            if (entry.getItem() instanceof BoatComponent component && component.addedConsumedFuel() > 0.0f) {
                fuelPerTick += component.addedConsumedFuel();
            }
        }
        return fuelPerTick;
    }

    public boolean isLowOnFuel() {
        return getFuel() < MAX_BASE_FUEL * 0.2;
    }

    public int getPowerLevel() {
        return this.boatEngine.getPowerLevel();
    }

    public void setPowerLevel(int powerLevel) {
        powerLevel = Math.max(powerLevel, 0);
        powerLevel = Math.min(powerLevel, MAX_POWER_LEVEL);
        this.boatEngine.setPowerLevel(powerLevel);
    }

    public boolean isHeatingUp() {
        return getOverheat() > 10;
    }
    public int getOverheat() {
        return this.boatEngine.getOverheat();
    }

    public void setOverheat(int overheat) {
        this.boatEngine.setOverheat(overheat);
    }

    public boolean isOverheating() {
        return getOverheat() > MAX_OVERHEAT;
    }

    public boolean isExperiencingHeavyLoad() {
        if (!engineIsRunning()) return false;
        if (this.getPowerLevel() > 3) {
            if (this.getPowerLevel() * 0.1 < boatEngine.getVelocity().horizontalLength()) return true;
        }
        return this.getPowerLevel() >= MAX_POWER_LEVEL - 2;
        //TODO: add heavy load, if boat goes too slow for power level
    }

    public boolean isSubmerged() {
        return this.boatEngine.isSubmerged();
    }

    public void setSubmerged(boolean isSubmerged) {
        if (isSubmerged == this.isSubmerged()) return;
        this.boatEngine.setSubmerged(isSubmerged);
        soundStateChange(SoundInstanceIdentifier.ENGINE_RUNNING_UNDERWATER);
    }

    public boolean breaksWhenSubmerged() {
        boolean hasWaterProofedArmorStacks = this.armorItems.stream().anyMatch(stack ->
                stack.getItem() instanceof BoatComponent component && component.waterProofesEngine());
        boolean hasWaterProofedEquippedStacks = this.heldItems.stream().anyMatch(stack ->
                stack.getItem() instanceof BoatComponent component && component.waterProofesEngine());
        return !hasWaterProofedArmorStacks && !hasWaterProofedEquippedStacks;
    }

    public float calculateThrustModifier(BoatEntity hookedBoatEntity) {
        if (hookedBoatEntity == null) return 0.0f;
        List<ItemStack> boatComponentStacks = new ArrayList<>();
        int passengerCount = hookedBoatEntity.getPassengerList().size() - 1;    // engine is passenger too
        int maxPassenger = ((BoatEntityInvoker) hookedBoatEntity).invokeGetMaxPassenger();
        int thrust = 1;

        this.armorItems.forEach(stack -> {
            if (stack.getItem() instanceof BoatComponent component && component.addedThrust() > 0.0f) {
                boatComponentStacks.add(stack);
            }
        });
        this.heldItems.forEach(stack -> {
            if (stack.getItem() instanceof BoatComponent component && component.addedThrust() > 0.0f) {
                boatComponentStacks.add(stack);
            }
        });
        for (ItemStack thrustModifierStack : boatComponentStacks) {
            thrust += ((BoatComponent) thrustModifierStack.getItem()).addedThrust();
        }
        float passengerDeficit = (float) passengerCount / maxPassenger;
        return thrust * MathHelper.lerp(passengerDeficit, 1.0f, 0.7f);
    }

    public boolean canEquipPart(ItemStack stack) {
        if (!(stack.getItem() instanceof BoatComponent)) return false;
        List<Item> flaggedParts = new ArrayList<>();
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
        return !flaggedParts.contains(stack.getItem());
    }

    public void initiateSoundStateChange() {
        List<SoundInstanceIdentifier> instances = new ArrayList<>();
        if (engineIsRunning()) instances.add(SoundInstanceIdentifier.ENGINE_RUNNING);
        if (isOverheating()) instances.add(SoundInstanceIdentifier.ENGINE_OVERHEATING);
        if (isSubmerged()) {
            instances.add(SoundInstanceIdentifier.ENGINE_RUNNING_UNDERWATER);
            instances.remove(SoundInstanceIdentifier.ENGINE_RUNNING);
            instances.remove(SoundInstanceIdentifier.ENGINE_OVERHEATING);
        }

        for (SoundInstanceIdentifier entry : instances) {
            soundStateChange(entry);
        }
    }

    private void soundStateChange(@NotNull SoundInstanceIdentifier soundInstance) {
        if (!(boatEngine.getWorld() instanceof ServerWorld serverWorld)) return;
        PlayerLookup.around(serverWorld, boatEngine.getPos(), 20).forEach(player -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(soundInstance.getIdentifier());
            buf.writeVarInt(this.boatEngine.getId());
            ServerPlayNetworking.send(player, BoatismS2C.CUSTOM_SOUND_INSTANCE_PACKET, buf);
        });
    }

    private void playSoundEvent(SoundEvent soundEvent) {
        playSoundEvent(soundEvent, 1.0f, 1.0f);
    }


    @SuppressWarnings("SameParameterValue")
    private void playSoundEvent(SoundEvent soundEvent, float volume, float pitch) {
        if (this.boatEngine.getWorld().isClient()) return;
        this.boatEngine.getWorld().playSound(null, boatEngine.getBlockPos(),
                soundEvent, SoundCategory.NEUTRAL, volume, pitch);
    }
}
