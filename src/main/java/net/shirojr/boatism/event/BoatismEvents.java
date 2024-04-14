package net.shirojr.boatism.event;

import net.shirojr.boatism.event.custom.CommandRegistrationEvents;
import net.shirojr.boatism.event.custom.HudEvents;
import net.shirojr.boatism.event.custom.KeyBindEvents;

public class BoatismEvents {
    public static void registerEvents() {
        CommandRegistrationEvents.register();
    }

    public static void registerClientEvents() {
        KeyBindEvents.register();
        HudEvents.register();
    }
}
