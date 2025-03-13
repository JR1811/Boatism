package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.item.custom.BaseEngineItem;
import net.shirojr.boatism.item.custom.FermenterBlockItem;
import net.shirojr.boatism.item.custom.FuelBucketItem;
import net.shirojr.boatism.item.custom.upgrade.BoatismEquipmentItem;
import net.shirojr.boatism.item.custom.upgrade.CanisterItem;
import net.shirojr.boatism.item.custom.upgrade.ExhaustItem;
import net.shirojr.boatism.item.custom.upgrade.PlatesItem;
import net.shirojr.boatism.util.LoggerUtil;

import java.util.ArrayList;
import java.util.List;

public interface BoatismItems {
    List<ItemStack> ALL_ITEMS = new ArrayList<>();

    FuelBucketItem FUEL_BUCKET = register("fuel_bucket",
            new FuelBucketItem(BoatismFluids.OIL.still(), new FabricItemSettings().maxCount(1)));
    BaseEngineItem BASE_ENGINE = register("base_engine",
            new BaseEngineItem(new FabricItemSettings().maxCount(1).fireproof()));
    BoatismEquipmentItem COMPONENT_EXHAUST = register("component_exhaust",
            new ExhaustItem(new FabricItemSettings().maxCount(1)));
    CanisterItem COMPONENT_CANISTER = register("component_canister",
            new CanisterItem(new FabricItemSettings().maxCount(1)));
    CanisterItem COMPONENT_CANISTER_STRAPPED = register("component_canister_strapped",
            new CanisterItem(new FabricItemSettings().maxCount(1)));
    BoatismEquipmentItem COMPONENT_PLATES = register("component_plates",
            new PlatesItem(new FabricItemSettings().maxCount(1)));

    FermenterBlockItem FERMENTER_BLOCK = register("fermenter", new FermenterBlockItem(BoatismBlocks.FERMENTER, new Item.Settings().maxCount(1)));


    private static <T extends Item> T register(String name, T item) {
        T registeredEntry = Registry.register(Registries.ITEM, Boatism.getId(name), item);
        ALL_ITEMS.add(new ItemStack(registeredEntry));
        return registeredEntry;
    }

    static void initialize() {
        LoggerUtil.devLogger("initialized items");
    }
}
