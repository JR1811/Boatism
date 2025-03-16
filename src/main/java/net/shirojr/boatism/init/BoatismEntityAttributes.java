package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

public interface BoatismEntityAttributes {

    static void initialize() {
        FabricDefaultAttributeRegistry.register(BoatismEntities.BOAT_ENGINE, BoatEngineEntity.setAttributes());
    }
}
