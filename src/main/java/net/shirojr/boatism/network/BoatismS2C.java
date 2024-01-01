package net.shirojr.boatism.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.instance.custom.*;
import net.shirojr.boatism.util.BoatEngineHandler;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.SoundInstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class BoatismS2C {
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(BoatismNetworkIdentifiers.SOUND_START.getPacketIdentifier(),
                BoatismS2C::handleSoundInstanceChangePackets);
        ClientPlayNetworking.registerGlobalReceiver(BoatismNetworkIdentifiers.SOUND_END_ALL.getPacketIdentifier(),
                BoatismS2C::handleClearAllSoundInstancesPackets);
    }

    private static void handleSoundInstanceChangePackets(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                         PacketByteBuf clientBuf, PacketSender packetSender) {
        int entityId = clientBuf.readVarInt();
        client.execute(() -> {
            LoggerUtil.devLogger("client sound packet received");
            if (client.world == null) return;
            if (!(client.world.getEntityById(entityId) instanceof BoatEngineEntity boatEngineEntity)) return;
            BoatEngineHandler handler = boatEngineEntity.getEngineHandler();
            List<SoundInstanceIdentifier> instances = new ArrayList<>();
            if (handler.engineIsRunning()) instances.add(SoundInstanceIdentifier.ENGINE_RUNNING);
            if (handler.isOverheating()) instances.add(SoundInstanceIdentifier.ENGINE_OVERHEATING);
            if (handler.isSubmerged()) {
                instances.add(SoundInstanceIdentifier.ENGINE_RUNNING_UNDERWATER);
                instances.remove(SoundInstanceIdentifier.ENGINE_RUNNING);
                instances.remove(SoundInstanceIdentifier.ENGINE_OVERHEATING);
            }
            instances.forEach(soundInstanceIdentifier -> {
                BoatismSoundInstance soundInstance;
                switch (soundInstanceIdentifier) {
                    case ENGINE_RUNNING -> soundInstance = new EngineRunningSoundInstance(boatEngineEntity);
                    case ENGINE_RUNNING_UNDERWATER -> soundInstance = new EngineSubmergedSoundInstance(boatEngineEntity);
                    case ENGINE_LOW_FUEL -> soundInstance = new EngineLowFuelSoundInstance(boatEngineEntity);
                    case ENGINE_LOW_HEALTH -> soundInstance = new EngineLowHealthSoundInstance(boatEngineEntity);
                    case ENGINE_OVERHEATING -> soundInstance = new EngineOverheatingSoundInstance(boatEngineEntity);
                    default -> {
                        LoggerUtil.LOGGER.error(String.format("Failed to play %s SoundInstance", soundInstanceIdentifier.getIdentifier().getPath()));
                        return;
                    }
                }
                BoatismClient.soundManager.start(soundInstanceIdentifier, soundInstance);
            });
        });
    }

    private static void handleClearAllSoundInstancesPackets(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                            PacketByteBuf clientBuf, PacketSender packetSender) {
        client.execute(() -> {
            BoatismClient.soundManager.stopAllSoundInstances();
        });
    }
}
