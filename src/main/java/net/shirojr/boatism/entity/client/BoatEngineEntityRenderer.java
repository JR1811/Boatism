package net.shirojr.boatism.entity.client;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import org.joml.Quaternionf;

public class BoatEngineEntityRenderer extends LivingEntityRenderer<BoatEngineEntity, BoatEngineEntityModel<BoatEngineEntity>> {
    private static final Identifier TEXTURE = new Identifier(Boatism.MODID, "textures/entity/boatengine.png");

    public BoatEngineEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BoatEngineEntityModel<>(ctx.getPart(BoatismClient.BOAT_ENGINE_LAYER)), 0.4f);
    }

    @Override
    public Identifier getTexture(BoatEngineEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BoatEngineEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        float tiltAngle = 45;

        if (livingEntity.getHookedBoatEntity().isPresent()) {
            BoatEntity boat = livingEntity.getHookedBoatEntity().get();
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(boat.getYaw())));

        }
        if (livingEntity.isResting()) {
            float rotationOffsetX = 0.0f;
            float rotationOffsetY = 0.43f;
            float rotationOffsetZ = 0.5f;

            matrixStack.translate(rotationOffsetX, rotationOffsetY, rotationOffsetZ);
            matrixStack.multiply(new Quaternionf().setAngleAxis((Math.PI / 180) * tiltAngle, 1.0f, 0.0f, 0.0f));
            matrixStack.translate(-rotationOffsetX, -rotationOffsetY, -rotationOffsetZ);
        }
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, light);
    }

    @Override
    public boolean shouldRender(BoatEngineEntity entity, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    protected void renderLabelIfPresent(BoatEngineEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        //super.renderLabelIfPresent(entity, text, matrices, vertexConsumers, light);
    }

    @Override
    protected void setupTransforms(BoatEngineEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        float scaleFactor = 1.5f;

        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta);
        if (entity.getHookedBoatEntity().isPresent()) {
            BoatEntity boat = entity.getHookedBoatEntity().get();
        }
        matrices.scale(scaleFactor, scaleFactor, scaleFactor);
    }
}
