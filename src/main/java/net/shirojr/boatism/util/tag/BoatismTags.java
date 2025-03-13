package net.shirojr.boatism.util.tag;

import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.shirojr.boatism.Boatism;

public class BoatismTags {
    public static class Entities {
        public static TagKey<EntityType<?>> NOT_HOOKABLE = createTag("not_hookable");
        @SuppressWarnings("SameParameterValue")
        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Boatism.getId(name));
        }

        public static void initialize() {
            // static initialisation
        }
    }

    public static class Fluids {
        public static TagKey<Fluid> OIL = createTag("oil");

        @SuppressWarnings("SameParameterValue")
        private static TagKey<Fluid> createTag(String name) {
            return TagKey.of(RegistryKeys.FLUID, Boatism.getId(name));
        }

        public static void initialize() {
            // static initialisation
        }
    }

    public static class Items {
        public static TagKey<Item> FERMENTABLE = createTag("fermentable");

        public static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Boatism.getId(name));
        }

        public static void initialize() {
            // static initialisation
        }
    }

    public static void initialize() {
        Entities.initialize();
        Fluids.initialize();
        Items.initialize();
    }
}
