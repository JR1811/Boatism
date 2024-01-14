package net.shirojr.boatism.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.handler.EntityHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
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
}
