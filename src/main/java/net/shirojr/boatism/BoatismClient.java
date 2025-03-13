package net.shirojr.boatism;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.shirojr.boatism.block.custom.client.FluidClientHandler;
import net.shirojr.boatism.block.entity.client.FermentBlockEntityModel;
import net.shirojr.boatism.block.entity.client.FermentBlockEntityRenderer;
import net.shirojr.boatism.entity.client.BoatEngineEntityModel;
import net.shirojr.boatism.entity.client.BoatEngineEntityRenderer;
import net.shirojr.boatism.init.*;
import net.shirojr.boatism.network.BoatismS2C;
import net.shirojr.boatism.screen.EngineControlScreen;
import net.shirojr.boatism.sound.BoatismSoundManager;

public class BoatismClient implements ClientModInitializer {
    public static BoatismSoundManager soundManager;
    public static final EntityModelLayer BOAT_ENGINE_LAYER =
            new EntityModelLayer(Boatism.getId("boat_engine_layer"), "main");
    public static final EntityModelLayer FERMENTER_LAYER =
            new EntityModelLayer(Boatism.getId("fermenter_layer"), "main");

    @Override
    public void onInitializeClient() {
        BoatismS2C.registerClientReceivers();
        BoatismEvents.registerClientEvents();
        BoatismColorProviders.initialize();
        FluidClientHandler.initialize();
        soundManager = new BoatismSoundManager();

        EntityRendererRegistry.register(BoatismEntities.BOAT_ENGINE, BoatEngineEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(BOAT_ENGINE_LAYER, BoatEngineEntityModel::getTexturedModelData);

        BlockEntityRendererFactories.register(BoatismBlockEntities.FERMENTER, FermentBlockEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FERMENTER_LAYER, FermentBlockEntityModel::getTexturedModelData);

        HandledScreens.register(BoatismScreenHandlers.ENGINE_CONTROL_SCREEN_HANDLER, EngineControlScreen::new);
    }
}
