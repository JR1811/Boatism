package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;

public record StoppedTrackingEnginePacket(int entityNetworkId, boolean force) implements CustomPayload {
    public static final Id<StoppedTrackingEnginePacket> IDENTIFIER = new Id<>(BoatismNetworkIdentifiers.SOUND_END_ENGINE.getId());

    public static final PacketCodec<RegistryByteBuf, StoppedTrackingEnginePacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, StoppedTrackingEnginePacket::entityNetworkId,
            PacketCodecs.BOOL, StoppedTrackingEnginePacket::force,
            StoppedTrackingEnginePacket::new
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
        if (client.world == null) return;
        if (!(client.world.getEntityById(entityNetworkId) instanceof BoatEngineEntity boatEngine)) return;
        BoatismClient.soundManager.stopAllSoundInstancesForBoatEngineEntity(boatEngine, force);
    }
}
