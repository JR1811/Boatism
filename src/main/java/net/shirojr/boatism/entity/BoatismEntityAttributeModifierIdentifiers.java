package net.shirojr.boatism.entity;

import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;

public enum BoatismEntityAttributeModifierIdentifiers {
    ENGINE_COMPONENT_ARMOR("component_armor_bonus");

    private final String name;

    BoatismEntityAttributeModifierIdentifiers(String name) {
        this.name = name;
    }

    public Identifier getId() {
        return Boatism.getId(name);
    }

    public Identifier getId(String suffix) {
        return Boatism.getId(name + suffix);
    }
}
