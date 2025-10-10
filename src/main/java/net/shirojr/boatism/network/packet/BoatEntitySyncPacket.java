package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class BoatEntitySyncPacket {
    private final int entityId;
    private final Optional<UUID> linkedEngine;

    public static final Identifier IDENTIFIER = BoatismNetworkIdentifiers.BOAT_ENTITY_SYNC.getId();

    public BoatEntitySyncPacket(int entityId, Optional<UUID> linkedEngine) {
        this.entityId = entityId;
        this.linkedEngine = linkedEngine;
    }

    public static void sendPacket(Collection<ServerPlayerEntity> targets, int entityId, Optional<UUID> linkedEngine) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entityId);
        buf.writeBoolean(linkedEngine.isPresent());
        linkedEngine.ifPresent(buf::writeUuid);

        for (ServerPlayerEntity player : targets) {
            ServerPlayNetworking.send(player, IDENTIFIER, buf);
        }
    }

    public static void handlePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        int entityId = buf.readVarInt();
        Optional<UUID> linkedEngine = buf.readBoolean() ? Optional.of(buf.readUuid()) : Optional.empty();

        client.execute(() -> {
            ClientWorld world = client.world;
            if (world == null) return;

            if (world.getEntityById(entityId) instanceof BoatEngineCoupler boat) {
                boat.boatism$setBoatEngineEntity(linkedEngine.orElse(null));
            }
        });
    }
}
