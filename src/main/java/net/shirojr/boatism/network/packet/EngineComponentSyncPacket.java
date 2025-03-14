package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.util.data.EngineComponent;

import java.util.List;

public record EngineComponentSyncPacket(int entityNetworkId, List<EngineComponent> componentList) implements CustomPayload {
    public static final Id<EngineComponentSyncPacket> IDENTIFIER = new Id<>(BoatismNetworkIdentifiers.BOAT_COMPONENT_SYNC.getId());

    public static final PacketCodec<RegistryByteBuf, EngineComponentSyncPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, EngineComponentSyncPacket::entityNetworkId,
            EngineComponent.CODEC.collect(PacketCodecs.toList()), EngineComponentSyncPacket::componentList,
            EngineComponentSyncPacket::new
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
        ClientWorld clientWorld = client.world;
        if (!(clientWorld.getEntityById(entityNetworkId) instanceof BoatEngineEntity boatEngine)) return;
        boatEngine.setMountedItemsFromComponentList(componentList);
    }
}
