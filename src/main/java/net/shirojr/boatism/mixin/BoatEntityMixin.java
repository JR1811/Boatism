package net.shirojr.boatism.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.BoatEngineCoupler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends VehicleEntity implements BoatEngineCoupler {
    @Unique
    private static final TrackedData<Optional<UUID>> BOAT_ENGINE_UUID = DataTracker.registerData(BoatEntityMixin.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

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
        ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid().ifPresent(uuid -> nbt.putUuid("BoatEngineUuid", uuid));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;handleBubbleColumn()V", shift = At.Shift.AFTER))
    private void boatism$test(CallbackInfo ci) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        boatism$getBoatEngineEntityUuid().ifPresent(uuid -> {
            if (!(serverWorld.getEntity(uuid) instanceof BoatEngineEntity boatEngine)) return;
            boatEngine.updateEnginePosition(boatEngine, Entity::setPos);
        });
    }
}
