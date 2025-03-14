package net.shirojr.boatism.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.shirojr.boatism.network.packet.*;

public class BoatismNetworkPayloads {

    static {
        registerC2S(PowerLevelChangePacket.IDENTIFIER, PowerLevelChangePacket.CODEC);
        registerC2S(OpenEngineInventoryPacket.IDENTIFIER, OpenEngineInventoryPacket.CODEC);

        registerS2C(StartSoundInstancePacket.IDENTIFIER, StartSoundInstancePacket.CODEC);
        registerS2C(EndAllSoundInstancesPacket.IDENTIFIER, EndAllSoundInstancesPacket.CODEC);
        registerS2C(StoppedTrackingEnginePacket.IDENTIFIER, StoppedTrackingEnginePacket.CODEC);
        registerS2C(EngineComponentSyncPacket.IDENTIFIER, EngineComponentSyncPacket.CODEC);
    }

    private static <T extends CustomPayload> void registerS2C(CustomPayload.Id<T> packetIdentifier, PacketCodec<RegistryByteBuf, T> codec) {
        PayloadTypeRegistry.playS2C().register(packetIdentifier, codec);
    }

    private static <T extends CustomPayload> void registerC2S(CustomPayload.Id<T> packetIdentifier, PacketCodec<RegistryByteBuf, T> codec) {
        PayloadTypeRegistry.playC2S().register(packetIdentifier, codec);
    }

    public static void initialize() {
        // static initialisation
    }
}
