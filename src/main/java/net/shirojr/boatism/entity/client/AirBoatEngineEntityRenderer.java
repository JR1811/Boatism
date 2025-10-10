package net.shirojr.boatism.entity.client;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.AirBoatEngineEntity;

public class AirBoatEngineEntityRenderer
        extends LivingEntityRenderer<AirBoatEngineEntity, AirBoatEngineEntityModel<AirBoatEngineEntity>> {
    private static final Identifier TEXTURE = Boatism.getId("textures/entity/air_boat_engine.png");

    public AirBoatEngineEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new AirBoatEngineEntityModel<>(ctx.getPart(BoatismClient.AIR_BOAT_ENGINE_LAYER)), 0.7f);
        this.addFeature(new EquipedPartFeatureRenderer<>(this, ctx.getItemRenderer()));
    }

    @Override
    public Identifier getTexture(AirBoatEngineEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(AirBoatEngineEntity boatEngineEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int light) {
        super.render(boatEngineEntity, f, g, matrixStack, vertexConsumerProvider, light);

        Random random = boatEngineEntity.getWorld().getRandom();
        //TODO: offset to main exhaust pipe
        if (boatEngineEntity.getEngineHandler().isHeatingUp() && random.nextInt(20) == 0) {
            boatEngineEntity.getWorld().addParticle(ParticleTypes.FLAME,
                    boatEngineEntity.getX() + random.nextFloat() * 0.1f,
                    boatEngineEntity.getY() + 0.4f + random.nextFloat() * 0.1f,
                    boatEngineEntity.getZ() + random.nextFloat() * 0.1f,
                    0.0f, 0.0f, 0.0f);
        }
        if (!boatEngineEntity.isRunning()) return;
        if (boatEngineEntity.getPowerLevel() > 0) {
            boatEngineEntity.getWorld().addParticle(ParticleTypes.BUBBLE,
                    boatEngineEntity.getX() + random.nextFloat() * 0.1f,
                    boatEngineEntity.getY() - 0.40f + random.nextFloat() * 0.1f,
                    boatEngineEntity.getZ() + random.nextFloat() * 0.1f,
                    0.0f, 0.0f, 0.0f);
        }
        //TODO: offset to main exhaust pipe
        if (boatEngineEntity.getPowerLevel() > 4 && random.nextInt(20) == 0) {
            boatEngineEntity.getWorld().addParticle(ParticleTypes.SMOKE,
                    boatEngineEntity.getX() + random.nextFloat() * 0.1f,
                    boatEngineEntity.getY() + 0.4f + random.nextFloat() * 0.1f,
                    boatEngineEntity.getZ() + random.nextFloat() * 0.1f,
                    0.0f, 0.0f, 0.0f);
        }
    }

    @Override
    public boolean shouldRender(AirBoatEngineEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    protected void renderLabelIfPresent(AirBoatEngineEntity entity, Text text, MatrixStack matrices,
                                        VertexConsumerProvider vertexConsumers, int light) {
        // super.renderLabelIfPresent(entity, text, matrices, vertexConsumers, light);
    }

    @Override
    protected void setupTransforms(AirBoatEngineEntity entity, MatrixStack matrices, float animationProgress,
                                   float bodyYaw, float tickDelta) {
        float scaleFactor = 1.5f;
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta);
        if (entity.getHookedBoatEntity().isPresent()) {
            BoatEntity boat = entity.getHookedBoatEntity().get();
        }
        matrices.scale(scaleFactor, scaleFactor, scaleFactor);
    }

}
