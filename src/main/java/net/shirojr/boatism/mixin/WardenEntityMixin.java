package net.shirojr.boatism.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WardenEntity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WardenEntity.class)
public class WardenEntityMixin {
    @Inject(method = "isValidTarget", at = @At(value = "HEAD"), cancellable = true)
    private void boatism$handleWardenTargetingForEngine(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // method might be run only once?
        if (!(entity instanceof BoatEngineEntity boatEngine)) return;
        if (!boatEngine.getEngineHandler().engineIsRunning()) {
            cir.setReturnValue(false);
        }
    }
}
