package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.LoggerUtil;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("SameParameterValue")
public class BoatismEntities {
    public static final EntityType<BoatEngineEntity> BOAT_ENGINE = register("boat_engine",
            FabricEntityTypeBuilder.<BoatEngineEntity>create(SpawnGroup.MISC, BoatEngineEntity::new)
                    .dimensions(EntityDimensions.changing(0.7f, 0.7f))
                    .trackedUpdateRate(1).spawnableFarFromPlayer().build());

    private static <E extends Entity, T extends EntityType<E>> T register(@NotNull String name, @NotNull T entityType) {
        return Registry.register(Registries.ENTITY_TYPE, Boatism.getId(name), entityType);
    }

    public static void initialize() {
        LoggerUtil.devLogger("initialized entity types");
    }
}
