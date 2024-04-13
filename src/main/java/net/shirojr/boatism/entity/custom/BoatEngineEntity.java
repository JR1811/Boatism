package net.shirojr.boatism.entity.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.BoatismEntities;
import net.shirojr.boatism.entity.animation.BoatismAnimation;
import net.shirojr.boatism.gamerule.BoatismGameRules;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.util.BoatEngineExplosionBehaviour;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.data.EngineComponent;
import net.shirojr.boatism.util.handler.BoatEngineHandler;
import net.shirojr.boatism.util.handler.EntityHandler;
import net.shirojr.boatism.util.nbt.BoatEngineNbtHelper;
import net.shirojr.boatism.util.nbt.NbtKeys;
import net.shirojr.boatism.util.sound.SoundInstanceIdentifier;
import net.shirojr.boatism.util.tag.BoatismTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BoatEngineEntity extends LivingEntity {
    private final SimpleInventory mountedInventory;
    private final PropertyDelegate propertyDelegate;
    private static final TrackedData<Integer> POWER_LEVEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> OVERHEAT = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<EulerAngle> ARM_ROTATION = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.ROTATION);
    private static final TrackedData<Boolean> SUBMERGED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RUNNING = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> FUEL = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> LOCKED = DataTracker.registerData(BoatEngineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final UUID COMPONENT_ARMOR_ID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");

    // public final AnimationState rightSpinAnimationState = new AnimationState();
    public final AnimationState leftSpinAnimationState = new AnimationState();
    public float spinAnimationTimeout = 0;

    @Nullable
    private UUID hookedBoatEntityUuid;
    @NotNull
    private final BoatEngineHandler engineHandler;
    private Vec3d previousLocation = Vec3d.ZERO;

    public BoatEngineEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.engineHandler = BoatEngineHandler.create(this);
        this.mountedInventory = new SimpleInventory(32);
        this.setNoGravity(true);
        this.setStepHeight(0.0f);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                // float values are multiplied by 100
                // to keep an acceptable amount of accuracy when rounding
                // and moving over int values for screen displaying purposes
                return switch (index) {
                    case 0 -> getPowerLevel();
                    case 1 -> Math.round(getFuel() * 100);
                    case 2 -> Math.round(getOverheat() * 100);
                    case 3 -> Math.round(engineHandler.getMaxOverHeatCapacity() * 100);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    setPowerLevel(value);
                }
            }

            @Override
            public int size() {
                return 4;
            }
        };
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
        this.dataTracker.startTracking(OVERHEAT, 0.0f);
        this.dataTracker.startTracking(ARM_ROTATION, new EulerAngle(0.0f, 5.0f, 0.0f));
        this.dataTracker.startTracking(SUBMERGED, false);
        this.dataTracker.startTracking(FUEL, 0.0f);
        this.dataTracker.startTracking(LOCKED, false);
        this.dataTracker.startTracking(RUNNING, false);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, Boatism.CONFIG.health)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0f)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 15.0f);
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
        BoatEngineNbtHelper.writeItemStacksToNbt(this.mountedInventory.getHeldStacks(), NbtKeys.MOUNTED_ITEMS, nbt);
        nbt.putBoolean(NbtKeys.IS_RUNNING, this.isRunning());
        nbt.putInt(NbtKeys.POWER_OUTPUT, this.getPowerLevel());
        nbt.putFloat(NbtKeys.OVERHEAT, this.getOverheat());
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
        if (nbt.contains(NbtKeys.MOUNTED_ITEMS)) {
            this.mountedInventory.clear();
            setMountedItemsFromItemStackList(BoatEngineNbtHelper.readItemStacksFromNbt(nbt, NbtKeys.MOUNTED_ITEMS));
            syncComponentListToTrackingClients();
        }
        this.setIsRunning(nbt.getBoolean(NbtKeys.IS_RUNNING));
        this.setPowerLevel(Math.min(nbt.getInt(NbtKeys.POWER_OUTPUT), BoatEngineHandler.MAX_POWER_LEVEL / 2));
        this.setOverheat(nbt.getFloat(NbtKeys.OVERHEAT));
        this.setArmRotation(new EulerAngle(nbt.getList(NbtKeys.ROTATION, NbtElement.FLOAT_TYPE)));
        this.setSubmerged(nbt.getBoolean(NbtKeys.IS_SUBMERGED));
        this.setFuel(nbt.getFloat(NbtKeys.FUEL));
        this.setLocked(nbt.getBoolean(NbtKeys.IS_LOCKED));
    }

    public void syncComponentListToTrackingClients() {
        if (this.getWorld().isClient()) return;
        PlayerLookup.tracking(this).forEach(player -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(this.getId());
            buf.writeVarInt(this.getMountedInventory().size());
            for (int i = 0; i < getMountedInventory().size(); i++) {
                buf.writeVarInt(i);
                buf.writeItemStack(getMountedInventory().getStack(i));
            }
            ServerPlayNetworking.send(player, BoatismNetworkIdentifiers.BOAT_COMPONENT_SYNC.getIdentifier(), buf);
        });
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
                modifyVelocity(boatEntity);
                if (getPowerLevel() > 3 && actualSpeed < 0.1) {
                    setOverheat(getOverheat() + 2);
                }
                if (this.isOnGround() && !this.isTouchingWater() && engineHandler.engineIsRunning()) {
                    setOverheat(getOverheat() + 2);
                }
            });
            this.previousLocation = this.getPos();
            if (isSubmerged()) {
                this.setAir(getMaxAir());
            }
        }

        this.engineHandler.setSubmerged(this.submergedInWater);
        this.engineHandler.incrementTick();
    }

    /**
     * Will modify the velocity of the boat if no Player is in the controlling position right now.
     * Otherwise, check {@link net.shirojr.boatism.mixin.BoatEntityMixin BoatEntityMixin} since the speed is applied
     * differently then.
     *
     * @param boatEntity Boat for the velocity changes
     */
    private void modifyVelocity(BoatEntity boatEntity) {
        if (boatEntity.getControllingPassenger() instanceof PlayerEntity) return;
        if (!isLogicalSideForUpdatingMovement()) return;
        if (boatEntity.isOnGround()) setOverheat(getOverheat() + 4);
        else {
            Vec3d newVelocity = boatEntity.getRotationVector().multiply(1.0, 0.0, 1.0).normalize()
                    .multiply(getPowerLevel() * 0.1).multiply(engineHandler.calculateThrustModifier(boatEntity));
            if (boatEntity.getVelocity().horizontalLength() < newVelocity.horizontalLength()) {
                boatEntity.addVelocity(newVelocity);
            }
        }
        boatEntity.velocityModified = true;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.isSneaking()) {
            if (stack.getItem() instanceof BoatEngineComponent) {
                if (engineHandler.canEquipPart(stack) && !mountedInventoryContains(stack)) {
                    addToMountedInventory(stack);
                    if (this.getWorld().isClient()) return ActionResult.SUCCESS;
                    LoggerUtil.devLogger(String.format("Equipped component %s on the engine", stack.getName()));
                    if (!player.isCreative()) stack.decrement(1);
                    player.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            BoatismSounds.BOAT_ENGINE_EQUIP, SoundCategory.NEUTRAL, 0.7f, 1.0f);
                    return ActionResult.SUCCESS;
                } else {
                    player.sendMessage(Text.translatable("warning.boatism.component_is_blocked"), true);
                }
            } else if (stack.isEmpty()) {
                if (getMountedInventorySize() > 0) {
                    EntityHandler.dropMountedInventory(this, false, true);
                    syncComponentListToTrackingClients();
                    if (this.getWorld().isClient()) return ActionResult.SUCCESS;
                    player.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            BoatismSounds.BOAT_ENGINE_EQUIP, SoundCategory.NEUTRAL, 0.7f, 1.0f);
                }
            }
        } else if (stack.isEmpty()) {
            if (!engineHandler.engineIsRunning()) engineHandler.startEngine();
            else engineHandler.stopEngine();
            LoggerUtil.devLogger(String.format("Engine is running: %s", engineHandler.engineIsRunning()));
            return ActionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        List<SoundInstanceIdentifier> identifierList = new ArrayList<>();
        syncComponentListToTrackingClients();
        sendPacketForStoppingAllSoundInstances(player);
        if (engineHandler.engineIsRunning()) identifierList.add(SoundInstanceIdentifier.ENGINE_RUNNING);
        if (engineHandler.isLowHealth()) identifierList.add(SoundInstanceIdentifier.ENGINE_LOW_HEALTH);
        if (engineHandler.isLowOnFuel()) identifierList.add(SoundInstanceIdentifier.ENGINE_LOW_FUEL);
        if (engineHandler.isSubmerged()) identifierList.add(SoundInstanceIdentifier.ENGINE_RUNNING_UNDERWATER);
        if (engineHandler.isHeatingUp()) identifierList.add(SoundInstanceIdentifier.ENGINE_OVERHEATING);
        identifierList.forEach(identifier -> sendPacketForSoundInstance(identifier, player));
        super.onStartedTrackingBy(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        sendPacketForStoppingAllSoundInstances(player);
        super.onStoppedTrackingBy(player);
    }

    private void sendPacketForSoundInstance(SoundInstanceIdentifier soundIdentifier, ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(this.getId());
        buf.writeIdentifier(soundIdentifier.getIdentifier());
        ServerPlayNetworking.send(player, BoatismNetworkIdentifiers.SOUND_START.getIdentifier(), buf);
        super.onStoppedTrackingBy(player);
    }

    private void sendPacketForStoppingAllSoundInstances(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(this.getId());
        buf.writeBoolean(true);
        ServerPlayNetworking.send(player, BoatismNetworkIdentifiers.SOUND_END_ENGINE.getIdentifier(), buf);
    }

    //region getter & setter
    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

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

    public SimpleInventory getMountedInventory() {
        return this.mountedInventory;
    }

    public int getMountedInventorySize() {
        int i = 0;
        for (ItemStack entry : getMountedInventory().getHeldStacks()) {
            if (entry.getItem() instanceof BoatEngineComponent) i++;
        }
        return i;
    }

    public void setMountedItemsFromItemStackList(List<ItemStack> mountedItems) {
        if (mountedItems.size() > getMountedInventory().size()) {
            LoggerUtil.devLogger("inventory size was bigger than expected", true, null);
            return;
        }
        for (int i = 0; i < mountedItems.size(); i++) {
            getMountedInventory().setStack(i, mountedItems.get(i));
        }
    }

    public void setMountedItemsFromComponentList(List<EngineComponent> components) {
        this.mountedInventory.clear();
        for (EngineComponent component : components) {
            this.mountedInventory.addStack(component.componentStack());
        }
    }

    public void addToMountedInventory(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BoatEngineComponent component)) return;
        EntityAttributeInstance armorAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (armorAttributeInstance == null || itemStack == null) return;
        this.getMountedInventory().addStack(component.getMountedItemStack(itemStack));
        if (this.getWorld().isClient()) return;
        changeArmorModifier(armorAttributeInstance, COMPONENT_ARMOR_ID);
    }

    public boolean mountedInventoryContains(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BoatEngineComponent component)) return false;
        return this.getMountedInventory().containsAny(mountedStack ->
                mountedStack.getItem().equals(component.getMountedItemStack(itemStack).getItem()));
    }

    public void changeArmorModifier(EntityAttributeInstance instance, UUID uuid) {
        instance.removeModifier(uuid);
        // removeModifier(modifier.getId());

        EntityAttributeModifier COMPONENT_ARMOR_BONUS = new EntityAttributeModifier(uuid, "Component armor bonus", engineHandler.getFullArmorValue(),
                EntityAttributeModifier.Operation.ADDITION);
        instance.addPersistentModifier(COMPONENT_ARMOR_BONUS);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
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
        LoggerUtil.devLogger("Tried to equip an item on a BoatEngineEntity!", true, null);
    }

    @Override
    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                BoatismSounds.BOAT_ENGINE_EQUIP, SoundCategory.NEUTRAL, 0.75f, 1f);
    }

    public void hookOntoBoatEntity(BoatEntity boatEntity) {
        if (boatEntity.getType().isIn(BoatismTags.Entities.NOT_HOOKABLE)) {
            LoggerUtil.devLogger("Entity was excluded from being able to hook an engine");
            return;
        }
        ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(this.getUuid());
        this.hookedBoatEntityUuid = boatEntity.getUuid();
        this.startRiding(boatEntity, true);
    }

    public void broadcastToAllPlayerPassengers(Text text, boolean overlay) {
        this.getHookedBoatEntity().ifPresent(boatEntity -> boatEntity.getPassengerList().forEach(entity -> {
            if (entity instanceof PlayerEntity player) {
                player.sendMessage(text, overlay);
            }
        }));
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
    public Iterable<ItemStack> getArmorItems() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
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

    public float getOverheat() {
        return this.dataTracker.get(OVERHEAT);
    }

    public void setOverheat(float overheat) {
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
        this.getHookedBoatEntity().ifPresent(boatEntity -> {
            for (Entity entry : boatEntity.getPassengerList()) {
                if (!(entry instanceof PlayerEntity player)) continue;
                if (player.isCreative()) continue;
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NAUSEA, 240, 1, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WITHER, 80, 1, false, false, true));
            }
        });
        boolean destructiveExplosion = serverWorld.getGameRules().getBoolean(BoatismGameRules.DESTRUCTIVE_ENGINE_EXPLOSION);
        for (ItemStack stack : getMountedInventory().getHeldStacks()) {
            if (!(stack.getItem() instanceof BoatEngineComponent component)) continue;
            if (component.getAdditionalArmor() > 0) destructiveExplosion = false;
        }
        serverWorld.createExplosion(this, Explosion.createDamageSource(serverWorld, this),
                new BoatEngineExplosionBehaviour(), this.getX(), this.getY(), this.getZ(), 4.0f, destructiveExplosion,
                World.ExplosionSourceType.NONE, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER,
                SoundEvents.ENTITY_GENERIC_EXPLODE);
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
