package net.shirojr.boatism.network;

import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;

public enum BoatismNetworkIdentifiers {
    SOUND_START("custom_sound_start_instance", LogicalSide.CLIENT),
    SOUND_END_ALL("custom_sound_stop_all_instances", LogicalSide.CLIENT),
    SCROLLED("scrolled", LogicalSide.SERVER);

    private final Identifier identifier;
    private final LogicalSide side;
    BoatismNetworkIdentifiers(String packetName, LogicalSide side) {
        this.identifier = new Identifier(Boatism.MODID, packetName);
        this.side = side;
    }

    public Identifier getPacketIdentifier() {
        return this.identifier;
    }

    public LogicalSide getEnvironmentSide() {
        return this.side;
    }

    enum LogicalSide {
        CLIENT, SERVER
    }
}
