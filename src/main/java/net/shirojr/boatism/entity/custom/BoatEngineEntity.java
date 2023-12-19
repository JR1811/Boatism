package net.shirojr.boatism.entity.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.world.World;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.entity.BoatismEntities;
import net.shirojr.boatism.mixin.BoatEntityInvoker;
import net.shirojr.boatism.network.BoatismS2C;
import net.shirojr.boatism.util.BoatComponent;
import net.shirojr.boatism.util.SoundInstanceHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoatEngineEntity extends LivingEntity {
    private final DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private static final TrackedData<Integer> POWER_OUTPUT = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<EulerAngle> ARM_ROTATION = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private static final TrackedData<Boolean> IS_SUBMERGED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HAS_LOW_FUEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ENGINE_IS_RESTING = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Nullable
    private BoatEntity hookedBoatEntity;

    public BoatEngineEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.hasNoGravity();
        this.setStepHeight(0.0f);
    }

    public BoatEngineEntity(World world, double x, double y, double z) {
        this(BoatismEntities.BOAT_ENGINE, world);
        this.setPosition(x, y, z);
    }

    public BoatEngineEntity(World world, BoatEntity hookedBoatEntity) {
        this(BoatismEntities.BOAT_ENGINE, world);
        this.hookOntoBoatEntity(hookedBoatEntity);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(POWER_OUTPUT, 0);
        this.dataTracker.startTracking(ARM_ROTATION, new EulerAngle(0.0f, 5.0f, 0.0f));
        this.dataTracker.startTracking(IS_SUBMERGED, false);
        this.dataTracker.startTracking(HAS_LOW_FUEL, false);
        this.dataTracker.startTracking(ENGINE_IS_RESTING, false);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f);
    }

    @Override
    public boolean shouldRenderName() {
        return super.shouldRenderName();
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        Identifier stateIdentifier = null;
        if (this.hasLowFuel()) stateIdentifier = SoundInstanceHelper.ENGINE_LOW_FUEL.getIdentifier();
        if (this.hasLowHealth()) stateIdentifier = SoundInstanceHelper.ENGINE_LOW_HEALTH.getIdentifier();
        if (this.isSubmerged()) stateIdentifier = SoundInstanceHelper.ENGINE_RUNNING_UNDERWATER.getIdentifier();
        if (stateIdentifier == null) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(stateIdentifier);
        ServerPlayNetworking.send(player, BoatismS2C.CUSTOM_SOUND_INSTANCE_PACKET, buf);
        super.onStartedTrackingBy(player);
    }

    //region getter & setter
    public Optional<BoatEntity> getHookedBoatEntity() {
        return Optional.ofNullable(this.hookedBoatEntity);
    }

    public int getPowerOutput() {
        return this.dataTracker.get(POWER_OUTPUT);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
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
    //endregion

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        switch (slot.getType()) {
            case HAND -> this.onEquipStack(slot, this.heldItems.set(slot.getEntitySlotId(), stack), stack);
            case ARMOR -> this.onEquipStack(slot, this.armorItems.set(slot.getEntitySlotId(), stack), stack);
        }
    }

    public void hookOntoBoatEntity(BoatEntity boatEntity) {
        this.hookedBoatEntity = boatEntity;
    }

    public float getMaxThrust() {
        boolean isHooked = getHookedBoatEntity().isPresent();
        if (!isHooked) return 0.0f;
        int passengerCount = getHookedBoatEntity().get().getPassengerList().size();

        List<ItemStack> boatComponentStacks = new ArrayList<>();
        this.getArmorItems().forEach(stack -> {
            if (stack.getItem() instanceof BoatComponent component && component.getThrust() > 0.0f) {
                boatComponentStacks.add(stack);
            }
        });
        this.getHandItems().forEach(stack -> {
            if (stack.getItem() instanceof BoatComponent component && component.getThrust() > 0.0f) {
                boatComponentStacks.add(stack);
            }
        });
        int thrust = 0;
        for (ItemStack thrustModifierStack : boatComponentStacks) {
            thrust += ((BoatComponent) thrustModifierStack.getItem()).getThrust();
        }
        BoatEntity boatEntity = getHookedBoatEntity().get();
        int maxPassenger = ((BoatEntityInvoker) boatEntity).invokeGetMaxPassenger();
        return thrust * ((maxPassenger - passengerCount) * 0.1f); //TODO: implement better balancing
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        return stack.getItem() instanceof BoatComponent;
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
        return this.getHealth() <= Boatism.CONFIG.boatValues.getLowHealthValue();
    }
}
