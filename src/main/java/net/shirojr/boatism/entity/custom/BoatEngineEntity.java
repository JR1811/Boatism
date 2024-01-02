package net.shirojr.boatism.entity.custom;

import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.BoatismEntities;
import net.shirojr.boatism.entity.animation.BoatismAnimation;
import net.shirojr.boatism.item.BoatismItems;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BoatEngineEntity extends LivingEntity {
    private DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private static final TrackedData<Integer> POWER_LEVEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> OVERHEAT = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<EulerAngle> ARM_ROTATION = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private static final TrackedData<Boolean> SUBMERGED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RUNNING = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> FUEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> LOCKED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public final AnimationState rightSpinAnimationState = new AnimationState();
    public final AnimationState leftSpinAnimationState = new AnimationState();
    public float spinAnimationTimeout = 0;

    @Nullable
    private UUID hookedBoatEntityUuid;
    @NotNull
    private final BoatEngineHandler engineHandler;
    private int previousPowerLevel = 0;
    private Vec3d previousLocation = Vec3d.ZERO;

    public BoatEngineEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.engineHandler = BoatEngineHandler.create(this, this.heldItems, this.armorItems);
        this.setNoGravity(true);
        this.setStepHeight(0.0f);
    }

    public BoatEngineEntity(World world, double x, double y, double z) {
        this(BoatismEntities.BOAT_ENGINE, world);
        this.setPosition(x, y, z);
    }

    public BoatEngineEntity(World world, BoatEntity hookedBoatEntity) {
        this(BoatismEntities.BOAT_ENGINE, world);
        this.setPos(hookedBoatEntity.getX(), hookedBoatEntity.getY(), hookedBoatEntity.getZ());
        this.hookOntoBoatEntity(hookedBoatEntity);
        this.previousLocation = hookedBoatEntity.getPos();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(POWER_LEVEL, 0);
        this.dataTracker.startTracking(OVERHEAT, 0);
        this.dataTracker.startTracking(ARM_ROTATION, new EulerAngle(0.0f, 5.0f, 0.0f));
        this.dataTracker.startTracking(SUBMERGED, false);
        this.dataTracker.startTracking(FUEL, 0.0f);
        this.dataTracker.startTracking(LOCKED, false);
        this.dataTracker.startTracking(RUNNING, false);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, Boatism.CONFIG.health)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f);
    }

    private void updateAnimationStates() {
        if (getPowerLevel() > 0) {
            if (spinAnimationTimeout <= 0) {
                this.spinAnimationTimeout = (BoatismAnimation.SPIN_DURATION_IN_SEC) * 20;
                this.leftSpinAnimationState.start(this.age);
            }
            --this.spinAnimationTimeout;
        } else {
            this.leftSpinAnimationState.stop();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        getHookedBoatEntityUuid().ifPresent(hookedBoatEntityUuid ->
                nbt.putUuid(NbtKeys.HOOKED_ENTITY, hookedBoatEntityUuid));
        BoatEngineNbtHelper.writeItemStacksToNbt(this.armorItems, NbtKeys.ARMOR_ITEMS, nbt);
        BoatEngineNbtHelper.writeItemStacksToNbt(this.heldItems, NbtKeys.HELD_ITEMS, nbt);
        nbt.putInt(NbtKeys.POWER_OUTPUT, this.getPowerLevel());
        nbt.putInt(NbtKeys.OVERHEAT, this.getOverheat());
        nbt.put(NbtKeys.ROTATION, this.getArmRotation().toNbt());
        nbt.putBoolean(NbtKeys.IS_SUBMERGED, this.isSubmerged());
        nbt.putFloat(NbtKeys.FUEL, this.getFuel());
        nbt.putBoolean(NbtKeys.IS_LOCKED, this.isLocked());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(NbtKeys.HOOKED_ENTITY)) {
            this.setHookedBoatEntity(nbt.getUuid(NbtKeys.HOOKED_ENTITY));
        }
        if (nbt.contains(NbtKeys.ARMOR_ITEMS)) {
            this.armorItems = BoatEngineNbtHelper.readItemStacksFromNbt(nbt, NbtKeys.ARMOR_ITEMS, 4);
        }
        if (nbt.contains("HandItems")) {
            this.heldItems = BoatEngineNbtHelper.readItemStacksFromNbt(nbt, NbtKeys.HELD_ITEMS, 2);
        }
        this.setPowerLevel(Math.min(nbt.getInt(NbtKeys.POWER_OUTPUT), BoatEngineHandler.MAX_POWER_LEVEL / 2));
        this.setOverheat(nbt.getInt(NbtKeys.OVERHEAT));
        this.setArmRotation(new EulerAngle(nbt.getList(NbtKeys.ROTATION, NbtElement.FLOAT_TYPE)));
        this.setSubmerged(nbt.getBoolean(NbtKeys.IS_SUBMERGED));
        this.setFuel(nbt.getFloat(NbtKeys.FUEL));
        this.setLocked(nbt.getBoolean(NbtKeys.IS_LOCKED));
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (this.getWorld().isClient()) {
            this.updateAnimationStates();
            return;
        }
        if (isRunning()) {
            this.getHookedBoatEntity().ifPresent(boatEntity -> {
                double actualSpeed = Math.max(0, this.previousLocation.distanceTo(this.getPos()));
                if (isLogicalSideForUpdatingMovement()) {
                    if (!boatEntity.isOnGround()) {
                        Vec3d newVelocity = boatEntity.getRotationVector().multiply(1.0, 0.0, 1.0).normalize()
                                .multiply(getPowerLevel() * 0.1).multiply(engineHandler.calculateThrustModifier(boatEntity));
                        if (boatEntity.getVelocity().horizontalLength() < newVelocity.horizontalLength()) {
                            boatEntity.addVelocity(newVelocity/* originalVelocity.multiply(newVelocity)*/);
                        }
                        // boatEntity.setVelocity(newVelocity/* originalVelocity.multiply(newVelocity)*/);
                    } else {
                        setOverheat(getOverheat() + 4);
                    }
                    boatEntity.velocityModified = true;
                    boatEntity.velocityDirty = true;
                }

                if (getPowerLevel() > 3 && actualSpeed < 0.1) {
                    setOverheat(getOverheat() + 2);
                }
            });
            this.previousLocation = this.getPos();
        }

        this.engineHandler.setSubmerged(this.submergedInWater);
        this.engineHandler.incrementTick();
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.getStackInHand(hand).isEmpty() && player.isSneaking()) {
            if (!this.getWorld().isClient()) {
                if (!engineHandler.engineIsRunning()) engineHandler.startEngine();
                else engineHandler.stopEngine();
                LoggerUtil.devLogger(String.format("Engine is running: %s", engineHandler.engineIsRunning()));
            }
            return ActionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        this.engineHandler.soundStateChange();
        super.onStartedTrackingBy(player);
    }

    //region getter & setter
    public Optional<BoatEntity> getHookedBoatEntity() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return Optional.empty();
        Entity entity = serverWorld.getEntity(this.hookedBoatEntityUuid);
        if (!(entity instanceof BoatEntity boatEntity)) return Optional.empty();
        return Optional.of(boatEntity);
    }

    public Optional<UUID> getHookedBoatEntityUuid() {
        return Optional.ofNullable(this.hookedBoatEntityUuid);
    }

    public void setHookedBoatEntity(UUID uuid) {
        this.hookedBoatEntityUuid = uuid;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
    }

    public void setArmorItems(DefaultedList<ItemStack> armorItems) {
        this.armorItems = armorItems;
    }

    public Iterable<ItemStack> getHeldItems() {
        return this.heldItems;
    }

    public void setHeldItems(DefaultedList<ItemStack> heldItems) {
        this.heldItems = heldItems;
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

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return BoatismSounds.BOAT_ENGINE_HIT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return BoatismSounds.BOAT_ENGINE_HIT;
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

    @Override
    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        super.onEquipStack(slot, oldStack, newStack);
        this.engineHandler.soundStateChange();
    }

    public void hookOntoBoatEntity(BoatEntity boatEntity) {
        ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(this.getUuid());
        this.hookedBoatEntityUuid = boatEntity.getUuid();
        this.startRiding(boatEntity, true);
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        return stack.getItem() instanceof BoatEngineComponent;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return super.canBeHitByProjectile();
    }

    @Override
    public boolean collidesWith(Entity other) {
        if (other instanceof BoatEntity) return false;
        return super.collidesWith(other);
    }

    @Override
    public boolean isAffectedBySplashPotions() {
        return false;
    }

    @Override
    public boolean isMobOrPlayer() {
        return false;
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public @NotNull BoatEngineHandler getEngineHandler() {
        return this.engineHandler;
    }

    public int getPowerLevel() {
        return this.dataTracker.get(POWER_LEVEL);
    }

    public void setPowerLevel(int level) {
        this.dataTracker.set(POWER_LEVEL, level);
    }

    public int getOverheat() {
        return this.dataTracker.get(OVERHEAT);
    }

    public void setOverheat(int overheat) {
        this.dataTracker.set(OVERHEAT, overheat);
    }

    public EulerAngle getArmRotation() {
        return this.dataTracker.get(ARM_ROTATION);
    }

    public void setArmRotation(EulerAngle armRotation) {
        this.dataTracker.set(ARM_ROTATION, armRotation);
    }

    public boolean isSubmerged() {
        return this.dataTracker.get(SUBMERGED);
    }

    public void setSubmerged(boolean isSubmerged) {
        this.dataTracker.set(SUBMERGED, isSubmerged);
    }

    public boolean isRunning() {
        return this.dataTracker.get(RUNNING);
    }

    public void setIsRunning(boolean shouldRun) {
        this.dataTracker.set(RUNNING, shouldRun);
    }

    public boolean isLocked() {
        return this.dataTracker.get(LOCKED);
    }

    public void setLocked(boolean resting) {
        this.dataTracker.set(LOCKED, resting);
    }

    public float getFuel() {
        return this.dataTracker.get(FUEL);
    }

    public void setFuel(float fuel) {
        this.dataTracker.set(FUEL, fuel);
    }

    public boolean hasLowHealth() {
        return this.getHealth() <= Boatism.CONFIG.lowHealth;
    }

    public void onOverheated() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        this.getHookedBoatEntity().ifPresent(boatEntity -> boatEntity.getPassengerList().forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                if (!(livingEntity instanceof PlayerEntity player) || !player.isCreative()) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NAUSEA, 240, 1, false, false, true));
                    livingEntity.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.WITHER, 80, 1, false, false, true));
                }
            }
        }));
        serverWorld.createExplosion(this, Explosion.createDamageSource(serverWorld, this), null,
                this.getX(), this.getY(), this.getZ(), 4.0f, true, World.ExplosionSourceType.NONE,
                ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
        this.kill();
    }

    public void removeBoatEngine(Entity entity) {
        if (!(entity instanceof BoatEntity boatEntity)) return;
        ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid().ifPresent(boatEngineEntity ->
                ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(null));
        this.discard();
    }

    @Override
    public void onRemoved() {
        getHookedBoatEntity().ifPresent(boatEntity -> ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(null));
        super.onRemoved();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        getHookedBoatEntity().ifPresent(boatEntity -> ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(null));
        super.onDeath(damageSource);
    }
}
