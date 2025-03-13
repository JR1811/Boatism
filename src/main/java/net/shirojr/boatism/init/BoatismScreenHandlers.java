package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;

public class BoatismScreenHandlers {
    public static ScreenHandlerType<EngineControlScreenHandler> ENGINE_CONTROL_SCREEN_HANDLER = register(
            "engine_control", new ExtendedScreenHandlerType<>(EngineControlScreenHandler::new));

    @SuppressWarnings("SameParameterValue")
    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, ScreenHandlerType<T> type) {
        return Registry.register(Registries.SCREEN_HANDLER, new Identifier(Boatism.MODID, name), type);
    }

    public static void initialize() {

    }
}
