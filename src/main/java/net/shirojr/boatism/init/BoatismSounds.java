package net.shirojr.boatism.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.shirojr.boatism.Boatism;

public interface BoatismSounds {
    SoundEvent BOAT_ENGINE_DEFAULT = of("boat_engine_default");
    SoundEvent BOAT_ENGINE_DEFAULT_FUEL_INJECTOR = of("boat_engine_default_fuel_injector");
    SoundEvent BOAT_ENGINE_UNDERWATER = of("boat_engine_underwater");
    SoundEvent BOAT_ENGINE_LOW_FUEL = of("boat_engine_low_fuel");
    SoundEvent BOAT_ENGINE_LOW_HEALTH = of("boat_engine_low_health");
    SoundEvent BOAT_ENGINE_HIT = of("boat_engine_hit");
    SoundEvent BOAT_ENGINE_POWER_UP = of("boat_engine_power_up");
    SoundEvent BOAT_ENGINE_POWER_DOWN = of("boat_engine_power_down");
    SoundEvent BOAT_ENGINE_START = of("boat_engine_start");
    SoundEvent BOAT_ENGINE_START_FAIL = of("boat_engine_start_fail");
    SoundEvent BOAT_ENGINE_STOP = of("boat_engine_stop");
    SoundEvent BOAT_ENGINE_HEAT = of("boat_engine_heat");
    SoundEvent BOAT_ENGINE_FILL_UP = of("boat_engine_pour");
    SoundEvent BOAT_ENGINE_EQUIP = of("boat_engine_equip");
    SoundEvent OIL_AMBIENT = of("oil_ambient");
    SoundEvent OIL_FLOWING = of("oil_flowing");
    SoundEvent OIL_SPLASH = of("oil_splash");


    static SoundEvent of(String id) {
        SoundEvent sound = SoundEvent.of(Boatism.getId(id));
        return Registry.register(Registries.SOUND_EVENT, Boatism.getId(id), sound);
    }

    static void initialize() {
        // static initialisation
    }
}