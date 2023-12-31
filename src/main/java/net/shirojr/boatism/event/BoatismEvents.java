package net.shirojr.boatism.event;

import net.shirojr.boatism.event.custom.CommandRegistrationEvents;

public class BoatismEvents {
    public static void registerEvents() {
        CommandRegistrationEvents.register();
    }

    public static void registerClientEvents() {

    }
}
