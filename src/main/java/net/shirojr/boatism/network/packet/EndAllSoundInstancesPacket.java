package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;

public record EndAllSoundInstancesPacket() implements CustomPayload {
    public static final Id<EndAllSoundInstancesPacket> IDENTIFIER = new Id<>(BoatismNetworkIdentifiers.SOUND_END_ALL.getId());

    public static final PacketCodec<RegistryByteBuf, EndAllSoundInstancesPacket> CODEC = PacketCodec.unit(new EndAllSoundInstancesPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }

    public void sendPacket(ServerPlayerEntity target) {
        ServerPlayNetworking.send(target, this);
    }

    public void handlePacket(ClientPlayNetworking.Context context) {
        BoatismClient.soundManager.stopAllSoundInstances(true);
    }
}
