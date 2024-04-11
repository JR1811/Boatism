package net.shirojr.boatism.util.tag;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.util.LoggerUtil;

public class BoatismTags {
    public static class Entities {
        public static TagKey<EntityType<?>> NOT_HOOKABLE = createTag("not_hookable");
        @SuppressWarnings("SameParameterValue")
        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier("boatism", name));
        }

        private static void register() {
            LoggerUtil.devLogger("initialized tags for entities");
        }
    }

    public static void initialize() {
        Entities.register();
        LoggerUtil.devLogger("initialized tags");
    }
}
