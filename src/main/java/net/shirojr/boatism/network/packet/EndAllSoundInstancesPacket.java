package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.minecraft.util.Identifier;

public class EndAllSoundInstancesPacket {
    public static final Identifier IDENTIFIER = BoatismNetworkIdentifiers.SOUND_END_ALL.getId();

    public EndAllSoundInstancesPacket() {}

    public static void sendPacket(ServerPlayerEntity target) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(target, IDENTIFIER, buf);
    }

    public static void handlePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        client.execute(() -> {
            BoatismClient.soundManager.stopAllSoundInstances(true);
        });
    }
}
