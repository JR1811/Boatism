package net.shirojr.boatism.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.handler.EntityHandler;
import net.shirojr.boatism.util.tag.BoatismTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow protected boolean firstUpdate;

    @Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Shadow public abstract boolean updateMovementInFluid(TagKey<Fluid> tag, double speed);

    @Inject(method = "remove", at = @At("HEAD"))
    private void boatism$removeHookedBoatEngineEntries(CallbackInfo ci) {
        EntityHandler.removePossibleBoatEngineEntry((Entity) (Object) this);
    }

    @Inject(method = "discard", at = @At("HEAD"))
    private void boatism$decoupleBoatEngineEntity(CallbackInfo ci) {
        EntityHandler.removePossibleBoatEngineEntry((Entity) (Object) this);
    }

    @Redirect(method = "addPassenger", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    protected boolean boatism$addPassengerMixin(List<Entity> list, Object passenger) {
        if ((Entity) (Object) this instanceof BoatEntity boatEntity && boatEntity.hasPassengers()) {
            for (int i = 0; i < boatEntity.getPassengerList().size(); i++) {
                if (boatEntity.getPassengerList().get(i) instanceof BoatEngineEntity) {
                    list.add(i, (Entity) passenger);
                    return true;
                }
            }
        }
        return list.add((Entity) passenger);
    }

    @Inject(method = "removeAllPassengers", at = @At("HEAD"), cancellable = true)
    private void boatism$engineRemovalCancel(CallbackInfo ci) {
        if ((Entity) (Object) this instanceof BoatEntity boatEntity && boatEntity.hasPassengers()) {
            if (boatEntity.getPassengerList().stream().noneMatch(entity -> entity instanceof BoatEngineEntity)) return;
            for (int i = 0; i < boatEntity.getPassengerList().size(); i++) {
                Entity passenger = boatEntity.getPassengerList().get(i);
                if (passenger instanceof BoatEngineEntity) continue;
                passenger.stopRiding();
            }
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "isInFluid", at = @At("RETURN"))
    private boolean boatism$isInFluid(boolean original) {
        Entity entity = (Entity) (Object) this;
        if (entity.getWorld().getFluidState(entity.getBlockPos()).isIn(BoatismTags.Fluids.OIL)) return true;
        return original;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPosition(DDD)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void boatism$resetFallDistanceInOil(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.fallDistance == 0.0f) return;
        if (!isLandingInOil(entity, movement, movementType)) return;
        entity.onLanding();
    }

    @ModifyReturnValue(method = "shouldSpawnSprintingParticles", at = @At("RETURN"))
    private boolean boatism$shouldSpawnParticlesInOil(boolean original) {
        return original && !isInOil();
    }

    @ModifyReturnValue(method = "updateWaterState", at = @At("RETURN"))
    private boolean boatism$updateWaterStateForOil(boolean original) {
        boolean updateMovement = updateMovementInFluid(BoatismTags.Fluids.OIL, 0.001);
        return original || !updateMovement;
    }

    @Unique
    private boolean isInOil() {
        return firstUpdate && fluidHeight.getDouble(BoatismTags.Fluids.OIL) > 0.0;
    }

    @Unique
    private static boolean isLandingInOil(Entity entity, Vec3d originalMovement, MovementType type) {
        originalMovement = entity.adjustMovementForSneaking(originalMovement, type);
        Vec3d movement = entity.adjustMovementForCollisions(originalMovement);
        double movementLength = movement.lengthSquared();
        if (movementLength > 1.0) return false;
        BlockHitResult hitResult = entity.getWorld().raycast(new RaycastContext(entity.getPos(), entity.getPos().add(movement),
                        RaycastContext.ShapeType.FALLDAMAGE_RESETTING, RaycastContext.FluidHandling.ANY, entity));
        HitResult.Type hitResultType =hitResult.getType();
        return !hitResultType.equals(HitResult.Type.MISS);
    }
}
