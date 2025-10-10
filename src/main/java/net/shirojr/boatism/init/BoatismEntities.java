package net.shirojr.boatism.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.entity.custom.AirBoatEngineEntity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.LoggerUtil;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("SameParameterValue")
public interface BoatismEntities {
    EntityType<BoatEngineEntity> BOAT_ENGINE = register("boat_engine",
            EntityType.Builder.<BoatEngineEntity>create(BoatEngineEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.7f, 0.7f).spawnableFarFromPlayer().build("boat_engine"));
    EntityType<AirBoatEngineEntity> AIR_BOAT_ENGINE = register("air_boat_engine",
            EntityType.Builder.<AirBoatEngineEntity>create(AirBoatEngineEntity::new, SpawnGroup.MISC)
                    .setDimensions(1.0f, 1.0f).spawnableFarFromPlayer().build("air_boat_engine"));

    private static <E extends Entity, T extends EntityType<E>> T register(@NotNull String name, @NotNull T entityType) {
        return Registry.register(Registries.ENTITY_TYPE, Boatism.getId(name), entityType);
    }

    static void initialize() {
        LoggerUtil.devLogger("initialized entity types");
    }
}