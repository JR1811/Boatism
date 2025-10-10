package net.shirojr.boatism.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.shirojr.boatism.network.packet.*;

public class BoatismS2C {
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(StartSoundInstancePacket.IDENTIFIER, StartSoundInstancePacket::handlePacket);

        ClientPlayNetworking.registerGlobalReceiver(EndAllSoundInstancesPacket.IDENTIFIER, EndAllSoundInstancesPacket::handlePacket);

        ClientPlayNetworking.registerGlobalReceiver(StoppedTrackingEnginePacket.IDENTIFIER, StoppedTrackingEnginePacket::handlePacket);

        ClientPlayNetworking.registerGlobalReceiver(EngineComponentSyncPacket.IDENTIFIER, EngineComponentSyncPacket::handlePacket);

        ClientPlayNetworking.registerGlobalReceiver(BoatEntitySyncPacket.IDENTIFIER, BoatEntitySyncPacket::handlePacket);
    }
}
