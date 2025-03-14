package net.shirojr.boatism.network;

import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;

public enum BoatismNetworkIdentifiers {
    SOUND_START("custom_sound_start_instance", TargetSide.CLIENT),
    SOUND_END_ENGINE("custom_sound_stop_engine_instances", TargetSide.CLIENT),
    SOUND_END_ALL("custom_sound_stop_all_instances", TargetSide.CLIENT),
    BOAT_COMPONENT_SYNC("component_sync", TargetSide.CLIENT),
    POWER_LEVEL_CHANGE("power_level_change", TargetSide.SERVER),
    OPEN_ENGINE_SCREEN("engine_screen_open", TargetSide.SERVER);

    private final Identifier identifier;
    private final TargetSide side;

    BoatismNetworkIdentifiers(String packetName, TargetSide side) {
        this.identifier = Boatism.getId(packetName);
        this.side = side;
    }

    public Identifier getId() {
        return this.identifier;
    }

    public TargetSide getTargetSide() {
        return this.side;
    }

    public enum TargetSide {
        CLIENT, SERVER
    }
}
