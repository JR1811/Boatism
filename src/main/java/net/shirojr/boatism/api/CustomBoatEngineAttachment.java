package net.shirojr.boatism.api;

import net.minecraft.entity.EntityDimensions;
import org.joml.Vector3f;

/**
 * <h1>Position for hooked Engine</h1>
 *
 * @apiNote Implement this interface to your custom Boat class to define the relative
 * attachment position of the engine.
 * @implNote Your custom Boat class should extend from {@link net.minecraft.entity.vehicle.BoatEntity BoatEntity}
 * to be able to even attach in the first place.
 */
public interface CustomBoatEngineAttachment {
    /**
     * Define the position of the Boat Engine when it is hooked up to your custom boat.<br><br>
     * If you need an example, check out the default positions defined
     * in {@link net.shirojr.boatism.mixin.BoatEntityMixin#boatism$attachmentPos(EntityDimensions)
     * BoatEntityMixin}
     *
     * @param dimensions Your entity dimensions (optional to use)
     * @return Relative position for the engine
     */
    Vector3f boatism$attachmentPos(EntityDimensions dimensions);
}
