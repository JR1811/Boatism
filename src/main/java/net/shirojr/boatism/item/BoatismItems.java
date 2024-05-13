package net.shirojr.boatism.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.block.BoatismFluids;
import net.shirojr.boatism.item.custom.*;
import net.shirojr.boatism.item.custom.upgrade.BoatismEquipmentItem;
import net.shirojr.boatism.item.custom.upgrade.CanisterItem;
import net.shirojr.boatism.item.custom.upgrade.ExhaustItem;
import net.shirojr.boatism.item.custom.upgrade.PlatesItem;
import net.shirojr.boatism.util.LoggerUtil;

import java.util.List;

public class BoatismItems {
    public static FuelBucketItem FUEL_BUCKET = register("fuel_bucket",
            new FuelBucketItem(BoatismFluids.OIL.still(), new FabricItemSettings().maxCount(1)));
    public static BaseEngineItem BASE_ENGINE = register("base_engine",
            new BaseEngineItem(new FabricItemSettings().maxCount(1).fireproof()));
    public static BoatismEquipmentItem COMPONENT_EXHAUST = register("component_exhaust",
            new ExhaustItem(new FabricItemSettings().maxCount(1)));
    public static CanisterItem COMPONENT_CANISTER = register("component_canister",
            new CanisterItem(new FabricItemSettings().maxCount(1)));
    public static CanisterItem COMPONENT_CANISTER_STRAPPED = register("component_canister_strapped",
            new CanisterItem(new FabricItemSettings().maxCount(1)));
    public static BoatismEquipmentItem COMPONENT_PLATES = register("component_plates",
            new PlatesItem(new FabricItemSettings().maxCount(1)));

    public static RegistryKey<ItemGroup> BOATISM_ITEM_GROUP = registerItemGroup("boatism",
            Text.translatable("itemgroup.boatism.boatism"), new ItemStack(BoatismItems.BASE_ENGINE));


    private static <T extends Item> T register(String name, T item) {
        Identifier identifier = new Identifier(Boatism.MODID, name);
        return Registry.register(Registries.ITEM, identifier, item);
    }

    private static RegistryKey<ItemGroup> registerItemGroup(String name, Text displayName, ItemStack displayItemStack) {
        ItemGroup group = FabricItemGroup.builder().icon(() -> displayItemStack).displayName(displayName).build();
        Identifier groupIdentifier = new Identifier(Boatism.MODID, name);
        Registry.register(Registries.ITEM_GROUP, groupIdentifier, group);
        return RegistryKey.of(RegistryKeys.ITEM_GROUP, groupIdentifier);
    }

    private static void initializeItemGroups() {
        ItemGroupEvents.modifyEntriesEvent(BOATISM_ITEM_GROUP).register(boatismEntries -> {
            List<ItemStack> boatismList = Registries.ITEM.stream()
                    .filter(item -> {
                                List<Item> ungroupedItems = List.of(BoatismItems.COMPONENT_CANISTER_STRAPPED);
                                if (!Registries.ITEM.getId(item).getNamespace().equals(Boatism.MODID)) return false;
                                return !ungroupedItems.contains(item);
                            }
                    )
                    .map(Item::getDefaultStack).toList();
            boatismEntries.addAll(boatismList);
        });
    }

    public static void initialize() {
        initializeItemGroups();
        LoggerUtil.devLogger("initialized items");
    }
}
