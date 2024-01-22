package net.shirojr.boatism.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttackWithOwnerGoal.class)
public class AttackWithOwnerGoalMixin {
    @Shadow
    @Final
    private TameableEntity tameable;

    @Inject(method = "canStart", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getAttacking()Lnet/minecraft/entity/LivingEntity;"),
            cancellable = true)
    private void boatism$wolfTargetSelectorChange(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity owner = tameable.getOwner();
        if (owner == null) return;
        LivingEntity target = owner.getAttacking();
        if (target instanceof BoatEngineEntity) {
            cir.setReturnValue(false);
        }
    }
}
