package net.shirojr.boatism.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.api.CustomBoatEngineAttachment;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismSounds;
import net.shirojr.boatism.item.custom.BaseEngineItem;
import net.shirojr.boatism.util.handler.EntityHandler;
import net.shirojr.boatism.util.nbt.BoatEngineNbtHelper;
import org.jetbrains.annotations.Nullable;
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
    private void boatism$injectInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(BOAT_ENGINE_UUID, Optional.empty());
    }

    @Override
    public void boatism$setBoatEngineEntity(@Nullable UUID boatEngineUuid) {
        this.dataTracker.set(BOAT_ENGINE_UUID, Optional.ofNullable(boatEngineUuid));
    }

    @Override
    public Optional<UUID> boatism$getBoatEngineEntityUuid() {
        return this.dataTracker.get(BOAT_ENGINE_UUID);
    }

    @Inject(method = "updatePaddles", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/vehicle/BoatEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;"))
    private void boatism$controlBoatSpeed(CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef f) {
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid().flatMap(engineUuid ->
                        EntityHandler.getBoatEngineEntityFromUuid(engineUuid, boatEntity.getWorld(), boatEntity.getPos(), 10))
                .ifPresent(boatEngine -> {
                    float baseSpeed = f.get();
                    float powerLevel = boatEngine.getPowerLevel() * 0.008f;
                    float thrust = baseSpeed + (powerLevel * boatEngine.getEngineHandler().calculateThrustModifier(boatEntity));
                    f.set(thrust);
                });
    }

    @Inject(method = "interact", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;startRiding(Lnet/minecraft/entity/Entity;)Z",
            shift = At.Shift.BEFORE),
            cancellable = true)
    private void boatism$equipEngineEntity(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getMainHandStack();
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        EntityHandler.engineLinkCleanUp(boatEntity);
        if (((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid().isPresent()) return;
        if (stack.getItem() instanceof BaseEngineItem) {
            if (!this.getWorld().isClient()) {
                // BoatEngineEntity engineEntity = new BoatEngineEntity(this.getWorld(), boatEntity);
                BoatEngineEntity engineEntity = BoatEngineNbtHelper.getBoatEngineEntityFromItemStack(stack, boatEntity);
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
    protected void boatism$getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor, CallbackInfoReturnable<Vec3d> cir) {
        if (passenger instanceof BoatEngineEntity && this instanceof CustomBoatEngineAttachment attachment) {
            cir.setReturnValue(attachment.boatism$attachmentPos(dimensions));
        }
    }

    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    protected void boatism$canAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> info) {
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        if (((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid().isEmpty()) return;
        int maxPassengers = ((BoatEntityInvoker) boatEntity).invokeGetMaxPassenger() + 1;
        if (boatEntity.getPassengerList().size() < maxPassengers) {
            info.setReturnValue(true);
        }
    }

    @Override
    public Vec3d boatism$attachmentPos(EntityDimensions dimensions) {
        if (this.getVariant() == BoatEntity.Type.BAMBOO) {
            return new Vec3d(0.0, dimensions.height() * 0.7, -1.2);
        }
        return new Vec3d(0.0, dimensions.height() / 3.0, -1.32);
    }

    @Shadow
    public abstract BoatEntity.Type getVariant();
}
