package net.shirojr.boatism.entity.client;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

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
    public void render(BoatEngineEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public boolean shouldRender(BoatEngineEntity entity, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z);
    }
}
