package net.shirojr.boatism.network.packet;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;

public class StoppedTrackingEnginePacket {
    public static final Identifier IDENTIFIER = BoatismNetworkIdentifiers.SOUND_END_ENGINE.getId();

    public static void sendPacket(ServerPlayerEntity player, int entityId, boolean force) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(entityId);
        buf.writeBoolean(force);
        ServerPlayNetworking.send(player, IDENTIFIER, buf);
    }

    public static void handlePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        int entityId = buf.readVarInt();
        boolean force = buf.readBoolean();

        client.execute(() -> {
            ClientWorld world = client.world;
            if (world == null) return;
            if (world.getEntityById(entityId) instanceof BoatEngineEntity boatEngine) {
                BoatismClient.soundManager.stopAllSoundInstancesForBoatEngineEntity(boatEngine, force);
            }
        });
    }
}
