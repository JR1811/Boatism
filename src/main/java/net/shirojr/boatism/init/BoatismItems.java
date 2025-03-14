package net.shirojr.boatism.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.EulerAngle;
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
            new FuelBucketItem(BoatismFluids.OIL.still(), new Item.Settings().maxCount(1)));
    BaseEngineItem BASE_ENGINE = register("base_engine",
            new BaseEngineItem(new Item.Settings().maxCount(1).fireproof()
                    .component(BoatismDataComponents.MOUNTED_ITEMS, new ArrayList<>())
                    .component(BoatismDataComponents.IS_RUNNING, false)
                    .component(BoatismDataComponents.POWER_OUTPUT, 0)
                    .component(BoatismDataComponents.OVERHEAT, 0f)
                    .component(BoatismDataComponents.ROTATION, new EulerAngle(0f, 0f, 0f))
                    .component(BoatismDataComponents.IS_SUBMERGED, false)
                    .component(BoatismDataComponents.FUEL, 0L)
                    .component(BoatismDataComponents.IS_LOCKED, false)
            ));
    BoatismEquipmentItem COMPONENT_EXHAUST = register("component_exhaust",
            new ExhaustItem(new Item.Settings().maxCount(1)));
    CanisterItem COMPONENT_CANISTER = register("component_canister",
            new CanisterItem(new Item.Settings().maxCount(1)));
    CanisterItem COMPONENT_CANISTER_STRAPPED = register("component_canister_strapped",
            new CanisterItem(new Item.Settings().maxCount(1)));
    BoatismEquipmentItem COMPONENT_PLATES = register("component_plates",
            new PlatesItem(new Item.Settings().maxCount(1)));

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
