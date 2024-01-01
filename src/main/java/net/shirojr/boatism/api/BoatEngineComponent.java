package net.shirojr.boatism.api;

import net.minecraft.item.Item;

import java.util.List;

/**
 * <h1>Custom Stats for BoatEngine Armor Items</h1>
 *
 * <h3>Usage</h3>
 *
 * Your custom Item class should extend from {@link net.minecraft.item.ArmorItem ArmorItem}
 * to be able to get equipped to the engine. All methods have a default implementation with no impact on the engine so
 * you don't have to override all methods.
 *
 * <h3>Information</h3>
 * <ul>
 *     <li>Access to the custom item rendering on the engine entity is not externally yet available.</li>
 *     <li>Limitation of only ArmorItem extending classes might be lifted in a future update</li>
 * </ul>
 *
 * @apiNote Implement this interface to your custom ArmorItem class to define the data, which improves or punishes
 * the engine's performance.
 */
public interface BoatEngineComponent {
    default List<Item> getConflictingParts() {
        return List.of();
    }
    default float addedThrust() {
        return 0.0f;
    }
    default float addedConsumedFuel() {
        return 0.0f;
    }
    default float addedFuelCapacity() {
        return 0.0f;
    }
    default float getAdditionalArmor() {
        return 0.0f;
    }
    default boolean waterProofsEngine() {
        return false;
    }
}
