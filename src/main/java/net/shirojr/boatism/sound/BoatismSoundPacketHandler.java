package net.shirojr.boatism.sound;

import net.minecraft.util.Identifier;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.instance.custom.*;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.sound.SoundInstanceIdentifier;

public class BoatismSoundPacketHandler {
    public static void handleSoundPacket(Identifier identifier, BoatEngineEntity boatEngineEntity) {
        SoundInstanceIdentifier.fromIdentifier(identifier).ifPresent(soundInstanceIdentifier -> {
            BoatismSoundInstance soundInstance;
            switch (soundInstanceIdentifier) {
                case ENGINE_RUNNING -> soundInstance = new EngineRunningSoundInstance(boatEngineEntity);
                case ENGINE_RUNNING_UNDERWATER -> soundInstance = new EngineSubmergedSoundInstance(boatEngineEntity);
                case ENGINE_LOW_FUEL -> soundInstance = new EngineLowFuelSoundInstance(boatEngineEntity);
                case ENGINE_LOW_HEALTH -> soundInstance = new EngineLowHealthSoundInstance(boatEngineEntity);
                case ENGINE_OVERHEATING -> soundInstance = new EngineOverheatingSoundInstance(boatEngineEntity);
                case NO_SOUND -> {
                    BoatismClient.soundManager.stopAllSoundInstancesForBoatEngineEntity(boatEngineEntity, true);
                    return;
                }
                default -> {
                    LoggerUtil.LOGGER.error(String.format("Failed to play %s SoundInstance", soundInstanceIdentifier.getIdentifier().getPath()));
                    return;
                }
            }
            BoatismClient.soundManager.start(soundInstanceIdentifier, soundInstance);
        });
    }
}
