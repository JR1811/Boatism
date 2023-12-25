package net.shirojr.boatism.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.util.LoggerUtil;

public class BoatismSounds {
    public static SoundEvent BOAT_ENGINE_DEFAULT = of("boat_engine_default");
    public static SoundEvent BOAT_ENGINE_UNDERWATER = of("boat_engine_underwater");
    public static SoundEvent BOAT_ENGINE_LOW_FUEL = of("boat_engine_low_fuel");
    public static SoundEvent BOAT_ENGINE_LOW_HEALTH = of("boat_engine_low_health");
    public static SoundEvent BOAT_ENGINE_HIT = of("boat_engine_hit");
    public static SoundEvent BOAT_ENGINE_POWER_UP = of("boat_engine_power_up");
    public static SoundEvent BOAT_ENGINE_POWER_DOWN = of("boat_engine_power_down");
    public static SoundEvent BOAT_ENGINE_START = of("boat_engine_start");
    public static SoundEvent BOAT_ENGINE_START_FAIL = of("boat_engine_start_fail");
    public static SoundEvent BOAT_ENGINE_STOP = of("boat_engine_stop");
    public static SoundEvent BOAT_ENGINE_HEAT = of("boat_engine_heat");
    public static SoundEvent BOAT_ENGINE_FILL_UP = of("boat_engine_pour");


    static SoundEvent of(String id) {
        SoundEvent sound = SoundEvent.of(new Identifier(Boatism.MODID, id));
        return Registry.register(Registries.SOUND_EVENT, new Identifier(Boatism.MODID, id), sound);
    }

    public static void initializeSounds() {
        LoggerUtil.LOGGER.info("Registering " + Boatism.MODID + " Sounds");
    }
}
