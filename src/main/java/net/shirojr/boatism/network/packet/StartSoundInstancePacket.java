package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.sound.BoatismSoundPacketHandler;

public class StartSoundInstancePacket {
    private final int entityNetworkId;
    private final Identifier identifier;

    public static final Identifier IDENTIFIER = BoatismNetworkIdentifiers.SOUND_START.getId();

    public StartSoundInstancePacket(int entityNetworkId, Identifier identifier) {
        this.entityNetworkId = entityNetworkId;
        this.identifier = identifier;
    }

    public static void sendPacket(ServerPlayerEntity target, int entityId, Identifier identifier) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entityId);
        buf.writeIdentifier(identifier);
        ServerPlayNetworking.send(target, IDENTIFIER, buf);
    }


    public static void handlePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        int entityNetworkId = buf.readVarInt();
        Identifier identifier = buf.readIdentifier();

        client.execute(() -> {
            if (client.world == null) return;
            if (!(client.world.getEntityById(entityNetworkId) instanceof BoatEngineEntity boatEngineEntity)) return;

            BoatismSoundPacketHandler.handleSoundPacket(identifier, boatEngineEntity);
        });
    }
}
