package net.shirojr.boatism.mixin;

import net.minecraft.entity.Entity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "onRemoved", at = @At("HEAD"))
    private void boatism$removeHookedBoatEngineEntries(CallbackInfo ci) {
        BoatEngineEntity.removeBoatEngineEntry((Entity) (Object) this);
    }

    @Inject(method = "kill", at = @At("HEAD"))
    private void boatism$decoupleBoatEngineEntity(CallbackInfo ci) {
        BoatEngineEntity.removeBoatEngineEntry((Entity) (Object) this);
    }
}
