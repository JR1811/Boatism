package net.shirojr.boatism.init;

import net.shirojr.boatism.event.custom.CommandRegistrationEvents;
import net.shirojr.boatism.event.custom.HudEvents;
import net.shirojr.boatism.event.custom.KeyBindEvents;

public interface BoatismEvents {
    static void registerCommonEvents() {
        CommandRegistrationEvents.register();
    }

    static void registerClientEvents() {
        KeyBindEvents.register();
        HudEvents.register();
    }
}
