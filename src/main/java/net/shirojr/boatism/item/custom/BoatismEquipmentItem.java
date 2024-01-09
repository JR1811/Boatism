package net.shirojr.boatism.item.custom;

import net.minecraft.item.Item;
import net.shirojr.boatism.api.BoatEngineComponent;

public abstract class BoatismEquipmentItem extends Item implements BoatEngineComponent {
    public BoatismEquipmentItem(Settings settings) {
        super(settings);
    }
}
