package net.shirojr.boatism.event.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.screen.EngineControlScreen;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;
import net.shirojr.boatism.util.data.EnginePartTexture;
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
        Optional<BoatEngineEntity> potentialBoatEngine = ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid()
                .flatMap(uuid -> EntityHandler.getBoatEngineEntityFromUuid(uuid, player.getWorld(), player.getPos(), 3));
        if (potentialBoatEngine.isEmpty()) return;

        BoatEngineEntity boatEngine = potentialBoatEngine.get();
        float heat = boatEngine.getOverheat() / boatEngine.getEngineHandler().getMaxOverHeatCapacity();

        int x = 20, y = context.getScaledWindowHeight() - 50;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        context.setShaderColor(1.0f, 1.0f, 1.0f, boatEngine.isRunning() ? 1.0f : 0.0f);
        renderEngineOverlay(context, EnginePartTexture.TURBINE, x, y, false);
        renderEngineOverlay(context, EnginePartTexture.BOTTOM, x, y, false);
        renderEngineOverlay(context, EnginePartTexture.MID, x, y, false);
        renderEngineOverlay(context, EnginePartTexture.TOP, x, y, false);

        context.setShaderColor(1.0f, 1.0f, 1.0f, heat);
        renderEngineOverlay(context, EnginePartTexture.TURBINE, x, y, true);
        renderEngineOverlay(context, EnginePartTexture.BOTTOM, x, y, true);
        renderEngineOverlay(context, EnginePartTexture.MID, x, y, true);
        renderEngineOverlay(context, EnginePartTexture.TOP, x, y, true);

        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderEngineOverlay(DrawContext context, EnginePartTexture part, int x, int y, boolean useHeatedTexture) {
        context.drawTexture(EngineControlScreen.TEXTURE, x + part.getXOffset(), y + part.getYOffset(),
                part.getU(useHeatedTexture), part.getV(), part.getWidth(), part.getHeight());
    }
}
