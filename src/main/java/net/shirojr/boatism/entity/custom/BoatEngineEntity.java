package net.shirojr.boatism.entity.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
import net.shirojr.boatism.util.BoatComponent;
import net.shirojr.boatism.util.BoatEngineHandler;
import net.shirojr.boatism.util.BoatEngineNbtHelper;
import net.shirojr.boatism.util.SoundInstanceHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BoatEngineEntity extends LivingEntity {
    private final DefaultedList<ItemStack> heldItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private static final TrackedData<Integer> POWER_OUTPUT = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.INTEGER); // implement power steps which are controllable with mouse wheel
    private static final TrackedData<EulerAngle> ARM_ROTATION = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private static final TrackedData<Boolean> IS_SUBMERGED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HAS_LOW_FUEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ENGINE_IS_RESTING = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN); // angle upwards (immunity on land)

    @Nullable
    private BoatEntity hookedBoatEntity;

    @NotNull
    private BoatEngineHandler engineHandler;

    public BoatEngineEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.engineHandler = BoatEngineHandler.create(this.heldItems, this.armorItems);
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
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (getHookedBoatEntity().isPresent()) {
            nbt.putUuid("HookedEntity", getHookedBoatEntity().get().getUuid());
        }
        BoatEngineNbtHelper.writeItemStacksToNbt(this.armorItems, "ArmorItems", nbt);
        BoatEngineNbtHelper.writeItemStacksToNbt(this.heldItems, "HandItems", nbt);
        nbt.putInt("PowerOutput", this.getPowerOutput());
        //FIXME: no eulerangle nbt handling yet
        nbt.putBoolean("IsSubmerged", this.isSubmerged());
        nbt.putBoolean("HasLowFuel", this.hasLowFuel());
        nbt.putBoolean("EngineIsResting", this.isResting());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("HookedEntity")) {
            this.setHookedBoatEntity(nbt.getUuid("HookedEntity"));
        }
        DefaultedList<ItemStack> armorList = BoatEngineNbtHelper.readItemStacksFromNbt(nbt, "ArmorItems");
        DefaultedList<ItemStack> handList = BoatEngineNbtHelper.readItemStacksFromNbt(nbt, "HandItems");
        this.setPowerOutput(nbt.getInt("PowerOutput"));
        //FIXME: no eulerangle nbt handling yet
        this.setSubmerged(nbt.getBoolean("IsSubmerged"));
        this.setLowFuel(nbt.getBoolean("HasLowFuel"));
        this.setResting(nbt.getBoolean("EngineIsResting"));
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public void tick() {
        super.tick();
        this.engineHandler.incrementTick();

        if (this.hookedBoatEntity == null) return;
        Vec3d vec3d = this.hookedBoatEntity.getPos().subtract(this.getPos());

        if (this.hookedBoatEntity != null && (vec3d.lengthSquared()) < 64.0) {
            this.setVelocity(this.getVelocity());
        }
        this.move(MovementType.SELF, this.getVelocity());
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

    /**
     * @param uuid Boat entity to bind the engine on
     * @return true, if entity has been successfully bound (can be ignored)
     */
    public boolean setHookedBoatEntity(UUID uuid) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(uuid);
            if (entity instanceof BoatEntity boatEntity) {
                this.hookedBoatEntity = boatEntity;
                return true;
            }
        }
        return false;
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
        this.hookedBoatEntity = boatEntity;
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
    public boolean isPushable() {
        return false;
    }

    public int getPowerOutput() {
        return this.dataTracker.get(POWER_OUTPUT);
    }

    public void setPowerOutput(int level) {
        this.dataTracker.set(POWER_OUTPUT, level);
    }

    public boolean isSubmerged() {
        return this.dataTracker.get(IS_SUBMERGED);
    }

    public void setSubmerged(boolean isSubmerged) {
        this.dataTracker.set(IS_SUBMERGED, isSubmerged);
    }

    public boolean isResting() {
        return this.dataTracker.get(ENGINE_IS_RESTING);
    }

    public void setResting(boolean resting) {
        this.dataTracker.set(ENGINE_IS_RESTING, resting);
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
}
