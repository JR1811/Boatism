package net.shirojr.boatism.util;

import java.util.Optional;
import java.util.UUID;

public interface BoatEngineCoupler {
    Optional<UUID> boatism$getBoatEngineEntityUuid();
    void boatism$setBoatEngineEntity(UUID boatEngineEntity);
}
