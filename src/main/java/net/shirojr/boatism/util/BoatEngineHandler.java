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
import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.mixin.BoatEntityInvoker;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.sound.BoatismSounds;

import java.util.ArrayList;
import java.util.List;

public class BoatEngineHandler {
    public static final int MAX_POWER_LEVEL = 9;
    public static final int MAX_BASE_FUEL = Boatism.CONFIG.maxBaseFuel;
    public static final int MAX_BASE_OVERHEAT = Boatism.CONFIG.maxBaseOverheat;

    private final BoatEngineEntity boatEngine;
    private boolean canPlayOverheat = true, canPlayLowFuel = true;

    private BoatEngineHandler(BoatEngineEntity boatEngine) {
        this.boatEngine = boatEngine;
    }

    public static BoatEngineHandler create(BoatEngineEntity boatEngine) {
        return new BoatEngineHandler(boatEngine);
    }

    public void incrementTick() {
        if (breaksWhenSubmerged() && isSubmerged()) stopEngine();
        if (handleFuel()) return;
        if (handleOverheating()) return;

        // LoggerUtil.devLogger(String.format("Fuel: %s | OverheatTicks: %s", getFuel(), getOverheat()));
    }


    private boolean handleFuel() {
        if (isLowOnFuel()) {
            if (canPlayLowFuel) {
                soundStateChange(List.of(SoundInstanceIdentifier.ENGINE_LOW_FUEL));
                canPlayLowFuel = false;
            }
        } else {
            if (!canPlayLowFuel) canPlayLowFuel = true;
        }
        if (!this.boatEngine.getWorld().isClient()) {
            if (getFuel() <= 0) {
                stopEngine();
                return true;
            }
            consumeFuel(1.0f);
        }

        return false;
    }

    private boolean handleOverheating() {
        if (!this.boatEngine.getWorld().isClient()) {
            LoggerUtil.devLogger("Max Fuel: %s | Fuel: %s | Max Heat: %s | Heat: %s"
                    .formatted(getMaxFuelCapacity(), getFuel(), getMaxOverHeatCapacity(),  getOverheat()));
            float currentOverHeat = 0;
            if (isExperiencingHeavyLoad()) {
                currentOverHeat += 2;
                setOverheat(getOverheat() + 2);
            } else if (getOverheat() > 0) {
                currentOverHeat -= 1;
                setOverheat(getOverheat() - 1);
            }
            setOverheat(getOverheat() + currentOverHeat);
        }
        if (isHeatingUp()) {
            if (canPlayOverheat) {
                soundStateChange(List.of(SoundInstanceIdentifier.ENGINE_OVERHEATING));
                canPlayOverheat = false;
            }
        } else {
            if (!canPlayOverheat) canPlayOverheat = true;
        }
        if (isOverheating()) {
            stopEngine();
            this.boatEngine.onOverheated();
            return true;
        }
        return false;
    }


    public void startEngine() {
        if (!engineCanStart()) {
            playSoundEvent(BoatismSounds.BOAT_ENGINE_START_FAIL);
            return;
        }
        playSoundEvent(BoatismSounds.BOAT_ENGINE_START);
        this.boatEngine.setIsRunning(true);
        soundStateChange(List.of(SoundInstanceIdentifier.ENGINE_RUNNING));
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
        fuel = Math.min(getMaxFuelCapacity(), fuel);
        this.boatEngine.setFuel(fuel);
    }

    public float getMaxFuelCapacity() {
        float maxCapacity = MAX_BASE_FUEL;

        for (ItemStack stack : getMountedItems()) {
            if (!(stack.getItem() instanceof BoatEngineComponent component)) continue;
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
        if (newFuelValue == getMaxFuelCapacity() + fuel) return fuel;
        playSoundEvent(BoatismSounds.BOAT_ENGINE_FILL_UP);
        if (newFuelValue > getMaxFuelCapacity()) {
            setFuel(getMaxFuelCapacity());
            return newFuelValue - getMaxFuelCapacity();
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
        for (ItemStack entry : getMountedItems()) {
            if (entry.getItem() instanceof BoatEngineComponent component) {
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

    public float getOverheat() {
        return this.boatEngine.getOverheat();
    }

    public void setOverheat(float overheat) {
        this.boatEngine.setOverheat(overheat);
    }

    public boolean isOverheating() {
        return getOverheat() > getMaxOverHeatCapacity();
    }

    public float getMaxOverHeatCapacity() {
        float maxCapacity = MAX_BASE_OVERHEAT;
        for (ItemStack stack : getMountedItems()) {
            if (!(stack.getItem() instanceof BoatEngineComponent component)) continue;
            maxCapacity += component.addedOverheatTolerance();
        }
        return maxCapacity;
    }

    public boolean isExperiencingHeavyLoad() {
        if (!engineIsRunning()) return false;
        if (this.getPowerLevel() > 3) {
            if (this.getPowerLevel() * 0.1 < boatEngine.getVelocity().horizontalLength()) return true;
        }
        return this.getPowerLevel() >= MAX_POWER_LEVEL - 2;
    }

    public boolean isSubmerged() {
        return this.boatEngine.isSubmerged();
    }

    public void setSubmerged(boolean isSubmerged) {
        if (isSubmerged == this.isSubmerged()) return;
        this.boatEngine.setSubmerged(isSubmerged);
        soundStateChange(List.of(SoundInstanceIdentifier.ENGINE_RUNNING_UNDERWATER));
    }

    public boolean breaksWhenSubmerged() {
        return getMountedItems().stream().noneMatch(stack ->
                stack.getItem() instanceof BoatEngineComponent component && component.waterProofsEngine());
    }

    public boolean isLowHealth() {
        return boatEngine.getHealth() < boatEngine.getMaxHealth() * 0.2;
    }

    public float getFullArmorValue() {
        float armor = 0;
        for (ItemStack entry : boatEngine.getMountedInventory().getHeldStacks()) {
            if (!(entry.getItem() instanceof BoatEngineComponent component)) continue;
            armor += component.getAdditionalArmor();
        }
        return armor;
    }

    public float calculateThrustModifier(BoatEntity hookedBoatEntity) {
        if (hookedBoatEntity == null) return 0.0f;
        List<ItemStack> boatComponentStacks = new ArrayList<>();
        int passengerCount = hookedBoatEntity.getPassengerList().size() - 1;    // engine is passenger too
        int maxPassenger = ((BoatEntityInvoker) hookedBoatEntity).invokeGetMaxPassenger();
        int thrust = 1;

        getMountedItems().forEach(stack -> {
            if (stack.getItem() instanceof BoatEngineComponent component && component.addedThrust() > 0.0f) {
                boatComponentStacks.add(stack);
            }
        });
        for (ItemStack thrustModifierStack : boatComponentStacks) {
            thrust += ((BoatEngineComponent) thrustModifierStack.getItem()).addedThrust();
        }
        float passengerDeficit = (float) passengerCount / maxPassenger;
        return thrust * MathHelper.lerp(passengerDeficit, 1.0f, 0.7f);
    }

    public boolean canEquipPart(ItemStack stack) {
        if (!(stack.getItem() instanceof BoatEngineComponent)) return false;
        List<Item> flaggedParts = new ArrayList<>();
        for (ItemStack entry : getMountedItems()) {
            if (entry.getItem() instanceof BoatEngineComponent component) {
                flaggedParts.addAll(component.getConflictingParts());
            }
        }
        return !flaggedParts.contains(stack.getItem());
    }

    public List<ItemStack> getMountedItems() {
        return boatEngine.getMountedInventory().getHeldStacks();
    }

    public void soundStateChange(List<SoundInstanceIdentifier> changedSoundList) {
        if (!(boatEngine.getWorld() instanceof ServerWorld serverWorld)) return;
        PlayerLookup.around(serverWorld, boatEngine.getPos(), 60).forEach(player -> {
            for (SoundInstanceIdentifier entry : changedSoundList) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarInt(this.boatEngine.getId());
                buf.writeIdentifier(entry.getIdentifier());
                ServerPlayNetworking.send(player, BoatismNetworkIdentifiers.SOUND_START.getIdentifier(), buf);
            }
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
