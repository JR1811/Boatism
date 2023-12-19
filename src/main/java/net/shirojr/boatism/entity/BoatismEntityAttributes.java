package net.shirojr.boatism.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.LoggerUtil;

public class BoatismEntityAttributes {

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(BoatismEntities.BOAT_ENGINE, BoatEngineEntity.setAttributes());

        LoggerUtil.devLogger("initialized entity attributes");
    }
}
