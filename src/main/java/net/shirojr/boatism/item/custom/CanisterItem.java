package net.shirojr.boatism.item.custom;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.shirojr.boatism.item.BoatismItems;
import net.shirojr.boatism.util.BoatComponent;

import java.util.List;

public class CanisterItem extends BoatismArmorItem implements BoatComponent {
    public CanisterItem(ArmorMaterial material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public List<Item> getConflictingParts() {
        return List.of(BoatismItems.COMPONENT_PLATES);
    }

    @Override
    public float addedFuelCapacity() {
        return 2000.0f;
    }

    @Override
    public float addedConsumedFuel() {
        return 0.2f;
    }
}
