package net.shirojr.boatism.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.world.World;
import net.shirojr.boatism.util.BoatComponent;

public class BoatEngineEntity extends LivingEntity {
    private final DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    public static final TrackedData<EulerAngle> ARM_ROTATION = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<Boolean> IS_SUBMERGED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> HAS_LOW_FUEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected BoatEngineEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ARM_ROTATION, new EulerAngle(1.0f, 2.0f, 3.0f));
        this.dataTracker.startTracking(IS_SUBMERGED, false);
        this.dataTracker.startTracking(HAS_LOW_FUEL, false);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        //TODO: implement sound instance packet S2C
        super.onStartedTrackingBy(player);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        return stack.getItem() instanceof BoatComponent;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        switch (slot.getType()) {
            case HAND -> this.onEquipStack(slot, this.heldItems.set(slot.getEntitySlotId(), stack), stack);
            case ARMOR -> this.onEquipStack(slot, this.armorItems.set(slot.getEntitySlotId(), stack), stack);
        }
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> this.heldItems.get(slot.getEntitySlotId());
            case ARMOR -> this.armorItems.get(slot.getEntitySlotId());
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public boolean isAffectedBySplashPotions() {
        return false;
    }

    @Override
    public boolean isMobOrPlayer() {
        return false;
    }

    public boolean isSubmerged() {
        return this.dataTracker.get(IS_SUBMERGED);
    }

    public boolean hasLowFuel() {
        return this.dataTracker.get(HAS_LOW_FUEL);
    }

    public boolean hasLowHealth() {
        return this.getHealth() <= 4.0f;
    }
}
