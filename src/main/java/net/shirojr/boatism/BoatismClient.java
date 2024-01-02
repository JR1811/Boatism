package net.shirojr.boatism;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.entity.BoatismEntities;
import net.shirojr.boatism.entity.client.BoatEngineEntityModel;
import net.shirojr.boatism.entity.client.BoatEngineEntityRenderer;
import net.shirojr.boatism.event.BoatismEvents;
import net.shirojr.boatism.network.BoatismS2C;
import net.shirojr.boatism.sound.BoatismSoundManager;

public class BoatismClient implements ClientModInitializer {
    public static BoatismSoundManager soundManager;
    public static final EntityModelLayer BOAT_ENGINE_LAYER =
            new EntityModelLayer(new Identifier(Boatism.MODID, "boat_engine_layer"), "main");

    @Override
    public void onInitializeClient() {
        BoatismS2C.registerClientReceivers();
        BoatismEvents.registerClientEvents();
        soundManager = new BoatismSoundManager();

        EntityRendererRegistry.register(BoatismEntities.BOAT_ENGINE, BoatEngineEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(BOAT_ENGINE_LAYER, BoatEngineEntityModel::getTexturedModelData);
    }
}
