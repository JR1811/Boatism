package net.shirojr.boatism.util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.boatism.entity.BoatismEntities;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityHandler {
    private EntityHandler() {
    }

    public static Optional<BoatEngineEntity> getBoatEngineEntityFromUuid(@Nullable UUID uuid, World world, Vec3d pos, int searchSize) {
        if (uuid == null) return Optional.empty();
        List<BoatEngineEntity> possibleEntities = world.getEntitiesByType(BoatismEntities.BOAT_ENGINE,
                Box.of(pos, searchSize, searchSize, searchSize),
                boatEngine -> boatEngine.getUuid().equals(uuid));
        if (possibleEntities.size() < 1) return Optional.empty();
        return Optional.ofNullable(possibleEntities.get(0));

    }
}
