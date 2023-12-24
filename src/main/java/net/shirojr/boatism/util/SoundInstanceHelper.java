package net.shirojr.boatism.util;

import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;

import java.util.Optional;

public enum SoundInstanceHelper {
    ENGINE_RUNNING("engine_running"),
    ENGINE_RUNNING_UNDERWATER("engine_underwater"),
    ENGINE_LOW_FUEL("engine_low_fuel"),
    ENGINE_LOW_HEALTH("engine_low_health"),
    ENGINE_OVERHEATING("engine_overheating");

    private final Identifier identifier;

    SoundInstanceHelper(String soundInstanceName) {
        this.identifier = new Identifier(Boatism.MODID, soundInstanceName);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public static Optional<SoundInstanceHelper> fromIdentifier(Identifier identifier) {
        for (SoundInstanceHelper instance : SoundInstanceHelper.values()) {
            if (instance.identifier.equals(identifier)) return Optional.of(instance);
        }
        return Optional.empty();
    }
}
