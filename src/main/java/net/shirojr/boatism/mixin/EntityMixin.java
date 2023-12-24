package net.shirojr.boatism.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.shirojr.boatism.util.BoatEngineCoupler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "onRemoved", at = @At("HEAD"))
    private void boatism$removeHookedBoatEngineEntries(CallbackInfo ci) {
        if (!((Entity) (Object) this instanceof BoatEntity boatEntity)) return;
        ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntity().ifPresent(boatEngineEntity ->
                ((BoatEngineCoupler) boatEntity).boatism$setBoatEngineEntity(null));
    }
}
