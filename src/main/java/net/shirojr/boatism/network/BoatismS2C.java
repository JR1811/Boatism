package net.shirojr.boatism.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.instance.custom.*;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.sound.SoundInstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class BoatismS2C {
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(BoatismNetworkIdentifiers.SOUND_START.getIdentifier(),
                BoatismS2C::handleSoundInstanceStartPackets);
        ClientPlayNetworking.registerGlobalReceiver(BoatismNetworkIdentifiers.SOUND_END_ALL.getIdentifier(),
                BoatismS2C::handleClearAllSoundInstancesPackets);
        ClientPlayNetworking.registerGlobalReceiver(BoatismNetworkIdentifiers.SOUND_END_ENGINE.getIdentifier(),
                BoatismS2C::handleStopTrackingEnginePackets);
        ClientPlayNetworking.registerGlobalReceiver(BoatismNetworkIdentifiers.BOAT_COMPONENT_SYNC.getIdentifier(),
                BoatismS2C::handleClientEngineComponentsSyncPackets);
    }

    private static void handleSoundInstanceStartPackets(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                        PacketByteBuf clientBuf, PacketSender packetSender) {
        int entityId = clientBuf.readVarInt();
        Identifier identifier = clientBuf.readIdentifier();
        client.execute(() -> {
            if (client.world == null) return;
            if (!(client.world.getEntityById(entityId) instanceof BoatEngineEntity boatEngineEntity)) return;
            SoundInstanceIdentifier.fromIdentifier(identifier).ifPresent(soundInstanceIdentifier -> {
                BoatismSoundInstance soundInstance;
                switch (soundInstanceIdentifier) {
                    case ENGINE_RUNNING -> soundInstance = new EngineRunningSoundInstance(boatEngineEntity);
                    case ENGINE_RUNNING_UNDERWATER ->
                            soundInstance = new EngineSubmergedSoundInstance(boatEngineEntity);
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
        });
    }

    private static void handleStopTrackingEnginePackets(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                        PacketByteBuf clientBuf, PacketSender packetSender) {
        int entityId = clientBuf.readVarInt();
        boolean force = clientBuf.readBoolean();
        client.execute(() -> {
            if (client.world == null) return;
            if (!(client.world.getEntityById(entityId) instanceof BoatEngineEntity boatEngine)) return;
            BoatismClient.soundManager.stopAllSoundInstancesForBoatEngineEntity(boatEngine, force);
        });
    }

    private static void handleClearAllSoundInstancesPackets(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                            PacketByteBuf clientBuf, PacketSender packetSender) {
        client.execute(() -> BoatismClient.soundManager.stopAllSoundInstances(true));
    }

    private static void handleClientEngineComponentsSyncPackets(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                                PacketByteBuf clientBuf, PacketSender packetSender) {
        int entityId = clientBuf.readVarInt();
        int componentListSize = clientBuf.readVarInt();
        List<BoatEngineEntity.EngineComponent> componentList = new ArrayList<>();
        for (int i = 0; i < componentListSize; i++) {
            componentList.add(new BoatEngineEntity.EngineComponent(clientBuf.readVarInt(), clientBuf.readItemStack()));
        }

        client.execute(() -> {
            if (client.world == null) return;
            ClientWorld clientWorld = client.world;
            if (!(clientWorld.getEntityById(entityId) instanceof BoatEngineEntity boatEngine)) return;
            boatEngine.setMountedItemsFromComponentList(componentList);
        });
    }
}
