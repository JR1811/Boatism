package net.shirojr.boatism.event.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;
import net.shirojr.boatism.util.data.EngineGui;
import net.shirojr.boatism.util.handler.EntityHandler;

import java.util.Optional;

public class HudEvents {
    public static void register() {
        HudRenderCallback.EVENT.register(HudEvents::handleEngineOverlay);
    }

    @Environment(EnvType.CLIENT)
    private static void handleEngineOverlay(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || !(player.getVehicle() instanceof BoatEntity boatEntity)) return;
        if (player.currentScreenHandler instanceof EngineControlScreenHandler) return;
        if (!Boatism.CONFIG.engineHudOverlay.shouldDisplay()) return;
        Optional<BoatEngineEntity> potentialBoatEngine = ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid()
                .flatMap(uuid -> EntityHandler.getBoatEngineEntityFromUuid(uuid, player.getWorld(), player.getPos(), 3));
        if (potentialBoatEngine.isEmpty()) return;

        BoatEngineEntity boatEngine = potentialBoatEngine.get();
        float heat = boatEngine.getOverheat() / boatEngine.getEngineHandler().getMaxOverHeatCapacity();
        int x = Boatism.CONFIG.engineHudOverlay.getX();
        int y = context.getScaledWindowHeight() - (36 + Boatism.CONFIG.engineHudOverlay.getY());

        EngineGui.renderEngineParts(context, EngineGui.getAllPartsInOrder(), x, y,
                boatEngine.isRunning() ? 1.0f : 0.0f, false);
        EngineGui.renderEngineParts(context, EngineGui.getAllPartsInOrder(), x, y,
                heat, true);
    }
}
