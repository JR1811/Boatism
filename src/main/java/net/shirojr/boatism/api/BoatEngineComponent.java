package net.shirojr.boatism.api;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.item.custom.upgrade.CanisterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Improve or Punish Engine Stats</h1>
 * <p>
 * Implement this interface to your custom Item class to define the data, which improves or punishes
 * the engine's performance. Check out {@link CanisterItem CanisterItem} if you need
 * an example.
 *
 * <h3>Usage</h3>
 * All methods have a default implementation with no impact on the engine so
 * you don't have to override all methods.
 */
public interface BoatEngineComponent {
    /**
     * If engine contains parts which conflict with this list, the engine won't accept equipping your item.
     */
    default List<Item> getConflictingParts() {
        return new ArrayList<>();
    }

    /**
     * This value will add to the maximal possible thrust of the engine
     */
    default float addedThrust() {
        return 0.0f;
    }

    /**
     * This value will lower the tick based overheat value, if the engine is overheating.
     */
    default float addedOverheatTolerance() {
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
     * This value will improve the armor of the engine.
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

    /**
     * This method can define an extra ItemStack, if the ItemStack from e.g. the inventory is not the same as the
     * one, which should be mounted on the engine, to be displayed.<br><br>
     * If you change this method, you migh also want to change
     * {@link BoatEngineComponent#getReturnedItemStack(ItemStack) getReturnedItemStack}
     *
     * @param originalStack Original ItemStack from e.g. the inventory
     * @return The new ItemStack which will be displayed on the engine
     */
    default ItemStack getMountedItemStack(ItemStack originalStack) {
        return originalStack;
    }

    /**
     * This method can define an extra ItemStack, if the ItemStack which is mounted on the engine is not the same as the
     * one, which should be returned if it will be taken off.<br><br>
     * If you change this method, you migh also want to change
     * {@link BoatEngineComponent#getMountedItemStack(ItemStack) getMountedItemStack}
     *
     * @param displayedStack Original ItemStack which is displayed on the engine
     * @return The new ItemStack which will be returned from the engine
     */
    default ItemStack getReturnedItemStack(ItemStack displayedStack) {
        return displayedStack;
    }

    /**
     * Allows for changing the rendering of the item on the engine. This way, Items can be adjusted individually.
     * The rendering of this item is implemented as an
     * {@link net.shirojr.boatism.entity.client.EquipedPartFeatureRenderer EquipedPartFeatureRenderer}
     * of the engine entity.<br><br>
     * @see CanisterItem CanisterItem
     *
     * @implNote make sure to push the MatrixStack to transform the item individually. The MatrixStack has the
     * pop call already defined after it's being rendered.
     *
     * @param boatEngineEntity engine, which has this component equipped
     * @param matrixStack      original MatrixStack from the component renderer
     * @return changed MatrixStack for the item feature renderer
     */
    default MatrixStack itemRenderTransform(BoatEngineEntity boatEngineEntity, MatrixStack matrixStack) {
        matrixStack.push();
        return matrixStack;
    }
}
