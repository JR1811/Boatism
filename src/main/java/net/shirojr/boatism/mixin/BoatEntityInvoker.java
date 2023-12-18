package net.shirojr.boatism.mixin;

import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BoatEntity.class)
public interface BoatEntityInvoker {
    @Invoker("getMaxPassengers")
    int invokeGetMaxPassenger();
}
