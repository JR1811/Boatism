package net.shirojr.boatism.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import net.shirojr.boatism.util.tag.BoatismTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInLava()Z"))
    private boolean boatism$isInOilTravel(LivingEntity instance, Operation<Boolean> original) {
        if (isInOilFluid(instance)) return true;
        return original.call(instance);
    }

    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getFluidHeight(Lnet/minecraft/registry/tag/TagKey;)D"))
    private double boatism$getFluidHeight(LivingEntity instance, TagKey<Fluid> tagKey, Operation<Double> original) {
        double lavaHeight = original.call(instance, tagKey);
        double oilHeight = instance.getFluidHeight(BoatismTags.Fluids.OIL);
        return Math.max(oilHeight, lavaHeight);
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInLava()Z"))
    private boolean boatism$isInOilMovement(LivingEntity instance, Operation<Boolean> original) {
        if (isInOilFluid(instance)) return true;
        return original.call(instance);
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getFluidHeight(Lnet/minecraft/registry/tag/TagKey;)D", ordinal = 1))
    private double boatism$getFluidHeightForOil(LivingEntity instance, TagKey<Fluid> tagKey, Operation<Double> original) {
        double waterHeight = original.call(instance, tagKey);
        double oilHeight = instance.getFluidHeight(BoatismTags.Fluids.OIL);
        return Math.max(waterHeight, oilHeight);
    }

    @Inject(method = "swimUpward", at = @At("HEAD"), cancellable = true)
    private void boatism$swimUpwardInOil(TagKey<Fluid> fluid, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (!livingEntity.getWorld().getFluidState(livingEntity.getBlockPos()).isIn(BoatismTags.Fluids.OIL)) return;
        livingEntity.setVelocity(livingEntity.getVelocity().add(0.0, 0.033f, 0.0));
        ci.cancel();
    }

    @Inject(method = "applyFluidMovingSpeed", at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/util/math/Vec3d;", shift = At.Shift.BEFORE), cancellable = true)
    private void boatism$oilMovingSpeed(double gravity, boolean falling, Vec3d motion, CallbackInfoReturnable<Vec3d> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (!isInOilFluid(livingEntity)) return;
        if (!falling) return;
        double maxFallingSpeed = 0.001;
        boolean fallingSpeed = Math.abs(motion.y - 0.005) >= maxFallingSpeed;
        boolean gravitySpeed = Math.abs(motion.y - gravity / 16.0) < maxFallingSpeed;
        double sinkingSpeed;
        if (fallingSpeed && gravitySpeed) {
            sinkingSpeed = -maxFallingSpeed;
        } else {
            sinkingSpeed = motion.y - gravity / 16.0;
        }
        cir.setReturnValue(new Vec3d(motion.x, sinkingSpeed, motion.z));
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean boatism$oilBreathing(LivingEntity instance, TagKey<Fluid> tagKey, Operation<Boolean> original) {
        return original.call(instance, tagKey) || instance.isSubmergedIn(BoatismTags.Fluids.OIL);
    }

    @Unique
    private static boolean isInOilFluid(LivingEntity livingEntity) {
        return livingEntity.getWorld().getFluidState(livingEntity.getBlockPos()).isIn(BoatismTags.Fluids.OIL);
    }
}
