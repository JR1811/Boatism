package net.shirojr.boatism.init;

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

import java.util.List;

public interface BoatismItemGroups {
    RegistryKey<ItemGroup> BOATISM_ITEM_GROUP = registerItemGroup("boatism",
            Text.translatable("itemgroup.boatism.boatism"), new ItemStack(BoatismItems.BASE_ENGINE));


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


    static void initialize() {
        // static initialisation
        initializeItemGroups();
    }
}
