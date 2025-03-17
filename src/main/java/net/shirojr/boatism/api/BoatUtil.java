package net.shirojr.boatism.api;

import net.minecraft.entity.Entity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Static Utility methods for usage with anything where a BoatEngine could be placed in
 */
@SuppressWarnings("unused")
public interface BoatUtil {
    static List<Entity> getPassengersWithoutEngine(Entity vehicleEntity) {
        List<Entity> entities = new ArrayList<>(vehicleEntity.getPassengerList());
        entities.removeIf(entity -> entity instanceof BoatEngineEntity);
        return entities;
    }

    static int getPassengersSize(Entity vehicleEntity) {
        return getPassengersWithoutEngine(vehicleEntity).size();
    }
}
