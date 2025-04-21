package net.shirojr.boatism.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
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
import net.shirojr.boatism.item.util.EnginePlacer;
import net.shirojr.boatism.network.packet.BoatEntitySyncPacket;
import net.shirojr.boatism.util.handler.EntityHandler;
import net.shirojr.boatism.util.nbt.BoatEngineDataHelper;
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
    @Nullable
    private UUID boatEngineUuid;

    public BoatEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void boatism$setBoatEngineEntity(@Nullable UUID boatEngineUuid) {
        this.boatEngineUuid = boatEngineUuid;
        if (this.getWorld().isClient()) return;
        new BoatEntitySyncPacket(this.getId(), Optional.ofNullable(this.boatEngineUuid)).sendPacket(PlayerLookup.tracking(this));
    }

    @Override
    @Nullable
    public UUID boatism$getBoatEngineEntityUuid() {
        return this.boatEngineUuid;
    }

    @Inject(method = "updatePaddles", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/vehicle/BoatEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;"))
    private void boatism$controlBoatSpeed(CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef f) {
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        UUID linkedBoatEngineUuid = ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid();
        Optional<BoatEngineEntity> linkedBoatEngine = EntityHandler.getBoatEngineEntityFromUuid(linkedBoatEngineUuid, boatEntity.getWorld(), boatEntity.getPos(), 10);
        linkedBoatEngine.ifPresent(boatEngine -> {
            float baseSpeed = f.get();
            float powerLevel = boatEngine.getPowerLevel() * 0.008f;
            float thrust = baseSpeed + (powerLevel * boatEngine.getEngineHandler().calculateThrustModifier(boatEntity));
            f.set(thrust);
        });
    }

    @Inject(method = "interact", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;startRiding(Lnet/minecraft/entity/Entity;)Z"),
            cancellable = true)
    private void boatism$equipEngineEntity(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getMainHandStack();
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        EntityHandler.engineLinkCleanUp(boatEntity);
        if (((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid() != null) return;
        if (stack.getItem() instanceof EnginePlacer) {
            if (!this.getWorld().isClient()) {
                BoatEngineEntity engineEntity = BoatEngineDataHelper.getBoatEngineEntity(stack, boatEntity);
                if (engineEntity != null) {
                    this.getWorld().spawnEntity(engineEntity);
                    boatism$setBoatEngineEntity(engineEntity.getUuid());
                    this.getWorld().playSound(null, boatEntity.getX(), boatEntity.getY(), boatEntity.getZ(),
                            BoatismSounds.BOAT_ENGINE_EQUIP, SoundCategory.NEUTRAL, 0.9f, 1.0f);
                    stack.decrement(1);
                }
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
        if (!nbt.contains("BoatEngineUuid")) return;
        ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(nbt.getUuid("BoatEngineUuid"));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void boatism$writeBoatEngineEntry(NbtCompound nbt, CallbackInfo ci) {
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        UUID linkedBoatEngineUuid = ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid();
        if (linkedBoatEngineUuid != null) {
            nbt.putUuid("BoatEngineUuid", linkedBoatEngineUuid);
        }
    }

    @Inject(method = "getPassengerAttachmentPos", at = @At("HEAD"), cancellable = true)
    protected void boatism$getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor, CallbackInfoReturnable<Vec3d> cir) {
        if (passenger instanceof BoatEngineEntity && this instanceof CustomBoatEngineAttachment attachment) {
            cir.setReturnValue(attachment.boatism$attachmentPos(this, dimensions).rotateY(-this.getYaw() * (float) (Math.PI / 180.0)));
        }
    }

    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    protected void boatism$canAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> info) {
        BoatEntity boatEntity = (BoatEntity) (Object) this;
        if (((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid() == null) return;
        int maxPassengers = ((BoatEntityInvoker) boatEntity).invokeGetMaxPassenger() + 1;
        if (boatEntity.getPassengerList().size() < maxPassengers) {
            info.setReturnValue(true);
        }
    }

    @Override
    public Vec3d boatism$attachmentPos(Entity vehicleEntity, EntityDimensions dimensions) {
        if (this.getVariant() == BoatEntity.Type.BAMBOO) {
            return new Vec3d(0.0, dimensions.height() * 0.7, -1.2);
        }
        return new Vec3d(0.0, dimensions.height() / 3.0, -1.32);
    }

    @Shadow
    public abstract BoatEntity.Type getVariant();
}
