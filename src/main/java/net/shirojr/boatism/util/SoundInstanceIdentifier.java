package net.shirojr.boatism.util;

import net.minecraft.sound.SoundCategory;
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
    private final SoundCategory category;

    SoundInstanceIdentifier(String soundInstanceName, SoundCategory category) {
        this.identifier = new Identifier(Boatism.MODID, soundInstanceName);
        this.category = category;
    }

    SoundInstanceIdentifier(String soundInstanceName) {
        this(soundInstanceName, SoundCategory.NEUTRAL);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public SoundCategory getCategory() {
        return this.category;
    }

    public static Optional<SoundInstanceIdentifier> fromIdentifier(Identifier identifier) {
        for (SoundInstanceIdentifier instance : SoundInstanceIdentifier.values()) {
            if (instance.identifier.equals(identifier)) return Optional.of(instance);
        }
        return Optional.empty();
    }
}
