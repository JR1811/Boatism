package net.shirojr.boatism.api;

import net.minecraft.entity.vehicle.BoatEntity;

import java.util.Optional;
import java.util.UUID;

public interface BoatEngineCoupler {
    /**
     * Get the hooked BoatEngineEntity by its UUID. If you don't know how to get an Entity from that, you can check
     * out the {@link net.shirojr.boatism.util.handler.EntityHandler EntityHandler} class.
     *
     * @return empty if no link has been found or the entry has been removed
     */
    Optional<UUID> boatism$getBoatEngineEntityUuid();

    /**
     * Set a new "BoatEntity to BoatEngineEntity" link. You can remove a hooked entry by passing in <b>null</b>.
     *
     * @param boatEngineEntity UUID of the BoatEngineEntity which should be linked to the Boat.
     * @apiNote If you want to actually hook the BoatEngine to the Boat, consider using
     * {@link net.shirojr.boatism.entity.custom.BoatEngineEntity#hookOntoBoatEntity(BoatEntity) hookOntoBoatEntity}
     * instead. This will set the hooked link, take care of tag entries and puts the engine into the passenger position.
     */
    void boatism$setBoatEngineEntity(UUID boatEngineEntity);
}
