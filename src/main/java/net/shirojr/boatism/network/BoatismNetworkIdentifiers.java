package net.shirojr.boatism.network;

import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;

public enum BoatismNetworkIdentifiers {
    SOUND_START("custom_sound_start_instance", TargetSide.CLIENT),
    SOUND_END_ENGINE("custom_sound_stop_engine_instances", TargetSide.CLIENT),
    SOUND_END_ALL("custom_sound_stop_all_instances", TargetSide.CLIENT),
    BOAT_COMPONENT_SYNC("component_sync", TargetSide.CLIENT),
    SCROLLED("scrolled", TargetSide.SERVER),
    OPEN_ENGINE_SCREEN("engine_screen_open", TargetSide.SERVER);

    private final Identifier identifier;
    private final TargetSide side;

    BoatismNetworkIdentifiers(String packetName, TargetSide side) {
        this.identifier = new Identifier(Boatism.MODID, packetName);
        this.side = side;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public TargetSide getTargetSide() {
        return this.side;
    }

    enum TargetSide {
        CLIENT, SERVER
    }
}
