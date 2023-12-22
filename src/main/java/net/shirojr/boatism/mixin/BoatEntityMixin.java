package net.shirojr.boatism.mixin;

import net.minecraft.entity.vehicle.BoatEntity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.BoatEngineCoupler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin implements BoatEngineCoupler {
    @Unique private BoatEngineEntity boatEngineEntity = null;

    @Override
    public void setBoatEngineEntity(BoatEngineEntity boatEngineEntity) {
        this.boatEngineEntity = boatEngineEntity;
    }

    @Override
    public BoatEngineEntity getBoatEngineEntity() {
        return this.boatEngineEntity;
    }
}
