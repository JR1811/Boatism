package net.shirojr.boatism.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.world.World;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.BoatEngineCoupler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends VehicleEntity implements BoatEngineCoupler {
    @Unique
    private BoatEngineEntity boatEngineEntity;

    public BoatEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void boatism$setBoatEngineEntity(BoatEngineEntity boatEngineEntity) {
        this.boatEngineEntity = boatEngineEntity;
    }

    @Override
    public Optional<BoatEngineEntity> boatism$getBoatEngineEntity() {
        return Optional.ofNullable(this.boatEngineEntity);
    }

    @Inject(method = "updatePassengerPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/VehicleEntity;updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", shift = At.Shift.AFTER))
    private void boatism$updateEnginePosition(Entity passenger, PositionUpdater positionUpdater, CallbackInfo ci) {
        BoatEntity boatEntity = (BoatEntity) (Object) this;

        ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntity().ifPresent(boatEngineEntity -> {
            //this.setVelocity(Vec3d.ZERO);
            boatEngineEntity.updateEnginePosition(boatEngineEntity,
                    (entity, x, y, z) -> entity.refreshPositionAndAngles(x, y, z, 0, 0));
            boatEngineEntity.updatePositionAndRotation();
        });
    }

    @Inject(method = "canCollide", at = @At("HEAD"), cancellable = true)
    private static void boatism$boatEngineCollision(Entity entity, Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (other instanceof BoatEngineEntity) {
            cir.setReturnValue(false);
        }
    }
}
