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
    private static final TrackedData<Float> FUEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> LOCKED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN); // angle upwards (immunity on land)
    @Nullable
    private UUID hookedBoatEntityUuid;
    @NotNull
    private final BoatEngineHandler engineHandler;

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
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(POWER_LEVEL, 0);
        this.dataTracker.startTracking(ARM_ROTATION, new EulerAngle(0.0f, 5.0f, 0.0f));
        this.dataTracker.startTracking(SUBMERGED, false);
        this.dataTracker.startTracking(FUEL, 0.0f);
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
        nbt.putFloat("Fuel", this.getFuel());
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
        this.setFuel(nbt.getFloat("Fuel"));
        this.setLocked(nbt.getBoolean("IsLocked"));
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        super.fall(heightDifference, onGround, state, landedPosition);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        //this.setVelocity(Vec3d.ZERO);
/*        this.updateEnginePosition(this, (entity, x, y, z) -> entity.refreshPositionAndAngles(x, y, z, 0, 0));
        this.updatePositionAndRotation();*/

        if (isRunning()) {
            this.getHookedBoatEntity().ifPresent(boatEntity -> {
                Vec3d originalVelocity = boatEntity.getVelocity();
                if (originalVelocity.length() <= 0 && boatEntity.getControllingPassenger() != null) {
                    originalVelocity = boatEntity.getControllingPassenger().getVelocity();
                }
                Vec3d newVelocity = boatEntity.getRotationVector().multiply(1.0, 0.0, 1.0).normalize().multiply(getPowerLevel() * 0.1);
                boatEntity.setVelocity(newVelocity/* originalVelocity.multiply(newVelocity)*/);
                boatEntity.velocityModified = true;
            });
        }

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
        Vector3f attachmentPos = new Vector3f(0.0f, 0.2f, -1.3f);
        return new Vec3d(attachmentPos.rotateY(-boatEntity.getYaw() * ((float) Math.PI / 180))).add(boatEntity.getPos());
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        this.engineHandler.initiateSoundStateChange();

        Identifier stateIdentifier = null;
        if (this.getEngineHandler().engineIsRunning())
            stateIdentifier = SoundInstanceIdentifier.ENGINE_RUNNING.getIdentifier();
        if (this.engineHandler.isLowOnFuel()) stateIdentifier = SoundInstanceIdentifier.ENGINE_LOW_FUEL.getIdentifier();
        if (this.hasLowHealth()) stateIdentifier = SoundInstanceIdentifier.ENGINE_LOW_HEALTH.getIdentifier();
        if (this.isSubmerged()) stateIdentifier = SoundInstanceIdentifier.ENGINE_RUNNING_UNDERWATER.getIdentifier();
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
        ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(this.getUuid());
        this.hookedBoatEntityUuid = boatEntity.getUuid();
        this.startRiding(boatEntity, true);
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

    public float getFuel() {
        return this.dataTracker.get(FUEL);
    }

    public void setFuel(float fuel) {
        this.dataTracker.set(FUEL, fuel);
    }

    public boolean hasLowHealth() {
        return this.getHealth() <= Boatism.CONFIG.boatValues.getLowHealthValue();
    }

    public void onOverheated() {
        if (!this.getEngineHandler().isOverheating()) return;

    }

    public static void removeBoatEngineEntry(Entity entity) {
        if (!(entity instanceof BoatEntity boatEntity)) return;
        ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid().ifPresent(boatEngineEntity ->
                ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(null));
    }

    @Override
    public void onRemoved() {
        getHookedBoatEntity().ifPresent(boatEntity -> ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(null));
        super.onRemoved();
    }
}
