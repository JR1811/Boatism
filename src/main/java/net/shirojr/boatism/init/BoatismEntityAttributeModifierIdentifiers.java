package net.shirojr.boatism.init;

import java.util.UUID;

public enum BoatismEntityAttributeModifierIdentifiers {
    ENGINE_COMPONENT_ARMOR("component_armor_bonus", UUID.fromString("6a1a40b6-52b5-4d73-8a3c-b1186f6d12c3"));

    private final String name;
    private final UUID uuid;

    BoatismEntityAttributeModifierIdentifiers(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }
}
