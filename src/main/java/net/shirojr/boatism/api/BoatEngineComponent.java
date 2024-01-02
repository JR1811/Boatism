package net.shirojr.boatism.api;

import net.minecraft.item.Item;

import java.util.List;

/**
 * <h1>Improve or Punish Engine Stats</h1>
 * <p>
 * Implement this interface to your custom ArmorItem class to define the data, which improves or punishes
 * the engine's performance. Check out {@link net.shirojr.boatism.item.custom.CanisterItem CanisterItem} if you need
 * and example.
 *
 * <h3>Usage</h3>
 * <p>
 * Your custom Item class should extend from {@link net.minecraft.item.ArmorItem ArmorItem}
 * to be able to get equipped to the engine. All methods have a default implementation with no impact on the engine so
 * you don't have to override all methods.
 *
 * <h3>Information</h3>
 * <ul>
 *     <li>Access to the custom item rendering on the engine entity is not externally yet available.</li>
 *     <li>Limitation of only ArmorItem extending classes might be lifted in a future update</li>
 * </ul>
 */
public interface BoatEngineComponent {
    /**
     * If engine contains parts which conflict with this list, the engine won't accept equipping your item.
     */
    default List<Item> getConflictingParts() {
        return List.of();
    }

    /**
     * This value will add to the maximal possible thrust of the engine
     */
    default float addedThrust() {
        return 0.0f;
    }

    /**
     * This value will add to the consumed fuel, if e.g. your custom Item is heavy.
     * Keep in mind that fuel is reduced per tick.
     */
    default float addedConsumedFuel() {
        return 0.0f;
    }

    /**
     * This value will add to the maximum capacity of the fuel engine.
     */
    default float addedFuelCapacity() {
        return 0.0f;
    }

    /**
     * This value will improve the armor of the engine. (not yet implemented!)
     */
    default float getAdditionalArmor() {
        return 0.0f;
    }

    /**
     * This value will enable your part to make the engine waterproof.
     * An engine needs all parts to be waterproof, to keep running underwater!
     */
    default boolean waterProofsEngine() {
        return false;
    }
}
