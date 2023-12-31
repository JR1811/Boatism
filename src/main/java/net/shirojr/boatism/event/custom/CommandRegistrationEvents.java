package net.shirojr.boatism.event.custom;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.shirojr.boatism.command.BoatEngineEntityCommand;

public class CommandRegistrationEvents {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(BoatEngineEntityCommand::register);
    }
}
