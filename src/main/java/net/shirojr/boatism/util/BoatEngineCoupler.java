package net.shirojr.boatism.util;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;

import java.util.Optional;

public interface BoatEngineCoupler {
    Optional<BoatEngineEntity> boatism$getBoatEngineEntity();
    void boatism$setBoatEngineEntity(BoatEngineEntity boatEngineEntity);
}
