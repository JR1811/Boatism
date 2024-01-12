package net.shirojr.boatism.api;

import net.minecraft.entity.Entity;
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
     * Defines the position of the Boat Engine when it is hooked up to your custom boat.
     * This will change only the position of the
     * {@link net.shirojr.boatism.entity.custom.BoatEngineEntity BoatEngineEntity} which is listed as a passenger.<br><br>
     * If you need an example, check out the default positions defined
     * in {@link net.shirojr.boatism.mixin.BoatEntityMixin#boatism$attachmentPos(EntityDimensions)
     * BoatEntityMixin}
     *
     * @param dimensions Your entity dimensions (optional to use)
     * @return Relative position for the engine
     * @apiNote If your custom {@link net.minecraft.entity.vehicle.BoatEntity BoatEntity} already overrides the
     * <b>getPassengerAttachmentPos()</b> method, Boatism's engine placement method might not work as intended.<br>
     *
     * <h3>Possible solutions</h3>
     *
     * <ul>
     *     <li>
     *         Call the super method after your passenger position changes. Then define the
     *         {@link net.shirojr.boatism.entity.custom.BoatEngineEntity BoatEngineEntity}'s position in this
     *         interface method like usually. This will support future dynamic engine position changes made by Boatism.
     *     </li>
     *     <li>
     *          [NOT RECOMMENDED] Don't use this interface method but check for the passenger entity to be a
     *          {@link net.shirojr.boatism.entity.custom.BoatEngineEntity BoatEngineEntity}
     *          and change the position manually. This won't support all position changes which are made by Boatism!
     *     </li>
     * </ul>
     */
    Vector3f boatism$attachmentPos(EntityDimensions dimensions);
}
