package net.shirojr.boatism.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.shirojr.boatism.api.CustomBoatEngineAttachment;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.item.custom.BaseEngineItem;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.util.BoatEngineCoupler;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends VehicleEntity implements BoatEngineCoupler, CustomBoatEngineAttachment {
    @Unique
    private static final TrackedData<Optional<UUID>> BOAT_ENGINE_UUID = DataTracker.registerData(BoatEntityMixin.class,
            TrackedDataHandlerRegistry.OPTIONAL_UUID);

    public BoatEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void boatism$injectDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(BOAT_ENGINE_UUID, Optional.empty());
    }

    @Override
    public void boatism$setBoatEngineEntity(@Nullable UUID boatEngineUuid) {
        this.dataTracker.set(BOAT_ENGINE_UUID, Optional.ofNullable(boatEngineUuid));
    }

    @Override
    public Optional<UUID> boatism$getBoatEngineEntityUuid() {
        return this.dataTracker.get(BOAT_ENGINE_UUID);
    }

    @Inject(method = "interact", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;startRiding(Lnet/minecraft/entity/Entity;)Z",
            shift = At.Shift.BEFORE),
            cancellable = true)
    private void boatism$equipEngineEntity(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        if (((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid().isPresent()) return;
        if (stack.getItem() instanceof BaseEngineItem engineItem) {
            if (!this.getWorld().isClient()) {
                BoatEngineEntity engineEntity = new BoatEngineEntity(this.getWorld(), boatEntity);
                this.getWorld().spawnEntity(engineEntity);
                this.getWorld().playSound(null, boatEntity.getX(), boatEntity.getY(), boatEntity.getZ(),
                        BoatismSounds.BOAT_ENGINE_EQUIP, SoundCategory.NEUTRAL, 0.9f, 1.0f);
                stack.decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "canCollide", at = @At("HEAD"), cancellable = true)
    private static void boatism$boatEngineCollision(Entity entity, Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (other instanceof BoatEngineEntity) {
            cir.setReturnValue(false);
        }
    }


    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void boatism$readBoatEngineEntry(NbtCompound nbt, CallbackInfo ci) {
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        if (!nbt.contains("BoatEngineUuid"))
            return;
        ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(nbt.getUuid("BoatEngineUuid"));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void boatism$writeBoatEngineEntry(NbtCompound nbt, CallbackInfo ci) {
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid()
                .ifPresent(uuid -> nbt.putUuid("BoatEngineUuid", uuid));
    }

    @Inject(method = "getPassengerAttachmentPos", at = @At("HEAD"), cancellable = true)
    protected void boatism$getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor,
                                                     CallbackInfoReturnable<Vector3f> info) {
        if (passenger instanceof BoatEngineEntity) {
            info.setReturnValue(boatism$attachmentPos(dimensions));
        }
    }

    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    protected void boatism$canAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> info) {
        if (this.getPassengerList().size() < 3 && ((BoatEngineCoupler) this).boatism$getBoatEngineEntityUuid().isPresent()) {
            info.setReturnValue(true);
        }
    }

    @Override
    public Vector3f boatism$attachmentPos(EntityDimensions dimensions) {
        if (this.getVariant() == BoatEntity.Type.BAMBOO) {
            return new Vector3f(0.0f, dimensions.height * 0.7f, -1.2f);
        }
        return new Vector3f(0.0f, dimensions.height / 3.0f, -1.32f);
    }

    @Shadow
    public abstract BoatEntity.Type getVariant();
}
