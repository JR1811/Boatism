package net.shirojr.boatism.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.instance.custom.*;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.SoundInstanceHelper;

public class BoatismS2C {
    public static final Identifier CUSTOM_SOUND_INSTANCE_PACKET = new Identifier(Boatism.MODID, "custom_sound_instance");

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(CUSTOM_SOUND_INSTANCE_PACKET, BoatismS2C::handleSoundInstancePackets);

    }

    private static void handleSoundInstancePackets(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                   PacketByteBuf clientBuf, PacketSender packetSender) {
        Identifier soundInstanceId = clientBuf.readIdentifier();
        int entityId = clientBuf.readVarInt();

        client.execute(() -> SoundInstanceHelper.fromIdentifier(soundInstanceId).ifPresent(soundInstanceHelper -> {
            if (client.world == null) return;
            if (!(client.world.getEntityById(entityId) instanceof BoatEngineEntity boatEngineEntity)) return;
            SoundInstance soundInstance;
            switch (soundInstanceHelper) {
                case ENGINE_RUNNING -> soundInstance = new EngineRunningSoundInstance(boatEngineEntity);
                case ENGINE_RUNNING_UNDERWATER -> soundInstance = new EngineSubmergedSoundInstance(boatEngineEntity);
                case ENGINE_LOW_FUEL -> soundInstance = new EngineLowFuelSoundInstance(boatEngineEntity);
                case ENGINE_LOW_HEALTH -> soundInstance = new EngineLowHealthSoundInstance(boatEngineEntity);
                case ENGINE_OVERHEATING -> soundInstance = new EngineOverheatingSoundInstance(boatEngineEntity);
                default -> {
                    LoggerUtil.LOGGER.error(String.format("Failed to play %s SoundInstance", soundInstanceId.getPath()));
                    return;
                }
            }

            // client.getSoundManager().play(soundInstance);
            BoatismClient.soundManager.start(soundInstanceHelper, soundInstance);
        }));
    }
}
