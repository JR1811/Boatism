package net.shirojr.boatism.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.item.custom.BoatismArmor;
import net.shirojr.boatism.item.material.BoatismArmorMaterial;
import net.shirojr.boatism.util.LoggerUtil;

public class BoatismItems {
    public static BoatismArmor EXHAUST_V1 = register("exhaust_v1",
            new BoatismArmor(BoatismArmorMaterial.BOAT_EXHAUST_V1, ArmorItem.Type.BOOTS,
                    new FabricItemSettings().maxCount(1)));


    public static <T extends Item> T register(String name, T item) {
        Identifier identifier = new Identifier(Boatism.MODID, name);
        return Registry.register(Registries.ITEM, identifier, item);
    }

    public static void initialize() {
        LoggerUtil.devLogger("initialized items");
    }
}
