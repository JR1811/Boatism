package net.shirojr.boatism.util.sound;

import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;

import java.util.Optional;

public enum SoundInstanceIdentifier {
    ENGINE_RUNNING("engine_running"),
    ENGINE_RUNNING_UNDERWATER("engine_underwater"),
    ENGINE_LOW_FUEL("engine_low_fuel"),
    ENGINE_LOW_HEALTH("engine_low_health"),
    ENGINE_OVERHEATING("engine_overheating"),
    NO_SOUND("cancel_sound_instances");

    private final Identifier identifier;

    SoundInstanceIdentifier(String soundInstanceName) {
        this.identifier = Boatism.getId(soundInstanceName);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public static Optional<SoundInstanceIdentifier> fromIdentifier(Identifier identifier) {
        for (SoundInstanceIdentifier instance : SoundInstanceIdentifier.values()) {
            if (instance.identifier.equals(identifier)) return Optional.of(instance);
        }
        return Optional.empty();
    }
}
