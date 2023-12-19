package net.shirojr.boatism.item.custom;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.shirojr.boatism.util.BoatComponent;

public class BoatismArmor extends ArmorItem implements BoatComponent {
    public BoatismArmor(ArmorMaterial material, Type type, Settings settings) {
        super(material, type, settings);
    }
}
