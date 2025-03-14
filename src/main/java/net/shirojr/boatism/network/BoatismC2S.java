package net.shirojr.boatism.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.shirojr.boatism.network.packet.OpenEngineInventoryPacket;
import net.shirojr.boatism.network.packet.PowerLevelChangePacket;

public class BoatismC2S {
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(PowerLevelChangePacket.IDENTIFIER, PowerLevelChangePacket::handlePacket);
        ServerPlayNetworking.registerGlobalReceiver(OpenEngineInventoryPacket.IDENTIFIER, OpenEngineInventoryPacket::handlePacket);
    }
}
