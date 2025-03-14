package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.sound.instance.custom.*;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.sound.SoundInstanceIdentifier;

public record StartSoundInstancePacket(int entityNetworkId, Identifier identifier) implements CustomPayload {
    public static final CustomPayload.Id<StartSoundInstancePacket> IDENTIFIER = new CustomPayload.Id<>(BoatismNetworkIdentifiers.SOUND_START.getId());

    public static final PacketCodec<RegistryByteBuf, StartSoundInstancePacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, StartSoundInstancePacket::entityNetworkId,
            Identifier.PACKET_CODEC, StartSoundInstancePacket::identifier,
            StartSoundInstancePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }

    public void sendPacket(ServerPlayerEntity target) {
        ServerPlayNetworking.send(target, this);
    }

    public void handlePacket(ClientPlayNetworking.Context context) {
        MinecraftClient client = context.client();
        if (client == null) return;
        if (client.world == null) return;
        if (!(client.world.getEntityById(entityNetworkId) instanceof BoatEngineEntity boatEngineEntity)) return;

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
