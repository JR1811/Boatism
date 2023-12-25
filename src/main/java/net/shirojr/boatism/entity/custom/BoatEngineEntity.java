package net.shirojr.boatism.entity.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.entity.BoatismEntities;
import net.shirojr.boatism.network.BoatismS2C;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public class BoatEngineEntity extends LivingEntity {
    private final DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private static final TrackedData<Integer> POWER_LEVEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.INTEGER); // implement power steps which are controllable with mouse wheel
    private static final TrackedData<EulerAngle> ARM_ROTATION = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private static final TrackedData<Boolean> SUBMERGED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RUNNING = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HAS_LOW_FUEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> LOCKED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN); // angle upwards (immunity on land)
    @Nullable
    private UUID hookedBoatEntityUuid;
    @NotNull
    private final BoatEngineHandler engineHandler;
    private int lerpTicks;
    private double x, y, z;
    private double boatYaw, boatPitch;

    public BoatEngineEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.engineHandler = BoatEngineHandler.create(this, this.heldItems, this.armorItems);
        this.setNoGravity(true);
        //this.setVelocity(Vec3d.ZERO);
        //this.velocityModified = true;
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
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(POWER_LEVEL, 0);
        this.dataTracker.startTracking(ARM_ROTATION, new EulerAngle(0.0f, 5.0f, 0.0f));
        this.dataTracker.startTracking(SUBMERGED, false);
        this.dataTracker.startTracking(HAS_LOW_FUEL, false);
        this.dataTracker.startTracking(LOCKED, false);
        this.dataTracker.startTracking(RUNNING, false);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        getHookedBoatEntityUuid().ifPresent(hookedBoatEntityUuid -> nbt.putUuid("HookedEntity", hookedBoatEntityUuid));
        BoatEngineNbtHelper.writeItemStacksToNbt(this.armorItems, "ArmorItems", nbt);
        BoatEngineNbtHelper.writeItemStacksToNbt(this.heldItems, "HandItems", nbt);
        nbt.putInt("PowerOutput", this.getPowerLevel());
        nbt.put("ArmRotation", this.getArmRotation().toNbt());
        nbt.putBoolean("IsSubmerged", this.isSubmerged());
        nbt.putBoolean("HasLowFuel", this.hasLowFuel());
        nbt.putBoolean("IsLocked", this.isLocked());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("HookedEntity")) {
            this.setHookedBoatEntity(nbt.getUuid("HookedEntity"));
        }
        DefaultedList<ItemStack> armorList = BoatEngineNbtHelper.readItemStacksFromNbt(nbt, "ArmorItems");
        DefaultedList<ItemStack> handList = BoatEngineNbtHelper.readItemStacksFromNbt(nbt, "HandItems");
        this.setPowerLevel(nbt.getInt("PowerOutput"));
        this.setArmRotation(new EulerAngle(nbt.getList("ArmRotation", NbtElement.FLOAT_TYPE)));
        this.setSubmerged(nbt.getBoolean("IsSubmerged"));
        this.setLowFuel(nbt.getBoolean("HasLowFuel"));
        this.setLocked(nbt.getBoolean("IsLocked"));
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        super.fall(heightDifference, onGround, state, landedPosition);
    }

    //entity.refreshPositionAndAngles(x, y, z, entity.getYaw(), entity.getPitch())

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (this.getWorld().isClient()) return;

        this.engineHandler.setSubmerged(this.submergedInWater);
        this.engineHandler.incrementTick();
    }

    public void updateEnginePosition(BoatEngineEntity passenger, PositionUpdater positionUpdater) {
        getHookedBoatEntity().ifPresent(boatEntity -> {
            Vec3d enginePos = enginePosition(boatEntity);
            positionUpdater.accept(passenger, enginePos.x, enginePos.y, enginePos.z);
            passenger.setYaw(boatEntity.getYaw());
        });
    }

    private Vec3d enginePosition(BoatEntity boatEntity) {
        Vector3f attachmentPos = new Vector3f(0.0f, 0.2f, -1.0f);
        return new Vec3d(attachmentPos.rotateY(-boatEntity.getYaw() * ((float) Math.PI / 180))).add(boatEntity.getPos());
    }

    public void updatePositionAndRotation() {
        getHookedBoatEntity().ifPresent(boatEntity -> {
            if (this.isLogicalSideForUpdatingMovement()) {
                this.lerpTicks = 0;
                this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
            }
            if (this.lerpTicks <= 0) {
                return;
            }
            this.lerpPosAndRotation(this.lerpTicks, this.x, this.y, this.z, this.boatYaw, this.boatPitch);
            --this.lerpTicks;
        });
    }

    @Override
    public double getLerpTargetX() {
        return this.lerpTicks > 0 ? this.x : this.getX();
    }

    @Override
    public double getLerpTargetY() {
        return this.lerpTicks > 0 ? this.y : this.getY();
    }

    @Override
    public double getLerpTargetZ() {
        return this.lerpTicks > 0 ? this.z : this.getZ();
    }

    @Override
    public float getLerpTargetPitch() {
        return this.lerpTicks > 0 ? (float) this.boatPitch : this.getPitch();
    }

    @Override
    public float getLerpTargetYaw() {
        return this.lerpTicks > 0 ? (float) this.boatYaw : this.getYaw();
    }


    @Override
    public void travel(Vec3d movementInput) {
        super.travel(movementInput);

    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        this.engineHandler.initiateSoundStateChange();

        Identifier stateIdentifier = null;
        if (this.getEngineHandler().engineIsRunning())
            stateIdentifier = SoundInstanceHelper.ENGINE_RUNNING.getIdentifier();
        if (this.hasLowFuel()) stateIdentifier = SoundInstanceHelper.ENGINE_LOW_FUEL.getIdentifier();
        if (this.hasLowHealth()) stateIdentifier = SoundInstanceHelper.ENGINE_LOW_HEALTH.getIdentifier();
        if (this.isSubmerged()) stateIdentifier = SoundInstanceHelper.ENGINE_RUNNING_UNDERWATER.getIdentifier();
        if (stateIdentifier == null) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(stateIdentifier);
        buf.writeVarInt(this.getId());
        ServerPlayNetworking.send(player, BoatismS2C.CUSTOM_SOUND_INSTANCE_PACKET, buf);
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

    public void hookOntoBoatEntity(BoatEntity boatEntity) {
        ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(this);
        this.hookedBoatEntityUuid = boatEntity.getUuid();
    }

    public float getMaxThrust() {
        return this.engineHandler.calculateMaxThrust(getHookedBoatEntity().orElse(null));
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        return stack.getItem() instanceof BoatComponent;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
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

    public boolean hasLowFuel() {
        return this.dataTracker.get(HAS_LOW_FUEL);
    }

    public void setLowFuel(boolean hasLowFuel) {
        this.dataTracker.set(HAS_LOW_FUEL, hasLowFuel);
    }

    public boolean hasLowHealth() {
        return this.getHealth() <= Boatism.CONFIG.boatValues.getLowHealthValue();
    }

    @Override
    public void onRemoved() {
        getHookedBoatEntity().ifPresent(boatEntity -> ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(null));
        super.onRemoved();
    }
}
