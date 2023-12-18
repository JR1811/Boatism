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


    static SoundEvent of(String id) {
        SoundEvent sound = SoundEvent.of(new Identifier(Boatism.MODID, id));
        return Registry.register(Registries.SOUND_EVENT, new Identifier(Boatism.MODID, id), sound);
    }

    public static void initializeSounds() {
        LoggerUtil.LOGGER.info("Registering " + Boatism.MODID + " Sounds");
    }
}
