package net.shirojr.boatism.entity;

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
                    .dimensions(EntityDimensions.changing(0.6f, 0.4f))
                    .spawnableFarFromPlayer().build());

    private static <E extends Entity, T extends EntityType<E>> T register(@NotNull String name, @NotNull T entityType) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(Boatism.MODID, name), entityType);
    }

    public static void initialize() {
        LoggerUtil.devLogger("initialized entity types");
    }
}
