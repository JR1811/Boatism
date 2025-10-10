package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.util.data.EngineComponent;

import java.util.ArrayList;
import java.util.List;

public class EngineComponentSyncPacket {
    public static final Identifier IDENTIFIER = BoatismNetworkIdentifiers.BOAT_COMPONENT_SYNC.getId();

    public static void sendPacket(ServerPlayerEntity player, int entityId, List<EngineComponent> components) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeVarInt(entityId);
        buf.writeVarInt(components.size());
        for (EngineComponent comp : components) {
            EngineComponent.write(buf, comp);
        }
        ServerPlayNetworking.send(player, IDENTIFIER, buf);
    }

    public static void handlePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        int entityId = buf.readVarInt();
        int size = buf.readVarInt();
        List<EngineComponent> components = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            components.add(EngineComponent.read(buf));
        }

        client.execute(() -> {
            ClientWorld world = client.world;
            if (world == null) return;
            if (world.getEntityById(entityId) instanceof BoatEngineEntity engine) {
                engine.setMountedItemsFromComponentList(components);
            }
        });
    }
}
