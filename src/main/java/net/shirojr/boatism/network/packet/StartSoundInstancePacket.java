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
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.sound.BoatismSoundPacketHandler;

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
        BoatismSoundPacketHandler.handleSoundPacket(identifier, boatEngineEntity);
    }
}
