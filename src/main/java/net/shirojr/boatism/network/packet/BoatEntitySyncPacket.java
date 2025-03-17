package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public record BoatEntitySyncPacket(int entityId, Optional<UUID> linkedEngine) implements CustomPayload {
    public static final Id<BoatEntitySyncPacket> IDENTIFIER = new Id<>(BoatismNetworkIdentifiers.BOAT_ENTITY_SYNC.getId());

    public static final PacketCodec<RegistryByteBuf, BoatEntitySyncPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BoatEntitySyncPacket::entityId,
            PacketCodecs.optional(Uuids.PACKET_CODEC), BoatEntitySyncPacket::linkedEngine,
            BoatEntitySyncPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }

    public void sendPacket(Collection<ServerPlayerEntity> targets) {
        targets.forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, this));
    }

    public void handlePacket(ClientPlayNetworking.Context context) {
        ClientWorld world = context.player().clientWorld;
        if (world == null || !(world.getEntityById(entityId) instanceof BoatEngineCoupler boat)) return;
        boat.boatism$setBoatEngineEntity(linkedEngine.orElse(null));
    }
}
