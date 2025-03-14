package net.shirojr.boatism.block.entity.client;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.block.custom.FermentBlock;
import net.shirojr.boatism.block.entity.custom.FermentBlockEntity;
import net.shirojr.boatism.util.LoggerUtil;
import org.jetbrains.annotations.Nullable;

public class FermentBlockEntityRenderer<T extends FermentBlockEntity> implements BlockEntityRenderer<T> {
    private final FermentBlockEntityModel model;
    private float prevLidOpenTick = 0;

    public FermentBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.model = new FermentBlockEntityModel(ctx.getLayerModelPart(BoatismClient.FERMENTER_LAYER));
    }

    @Override
    public void render(FermentBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        float normalizedLidOpenState = getNormalizedLidOpenState(blockEntity, tickDelta);
        this.prevLidOpenTick = blockEntity.getLidOpeningTick();

        matrices.push();
        matrices.translate(0.5, 0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(blockEntity.getCachedState().get(FermentBlock.FACING).asRotation()));
        //matrices.scale(1, -1, 1);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(Boatism.getId("textures/entity/fermenter.png")));
        this.model.render(normalizedLidOpenState, tickDelta, matrices, vertexConsumer, light, overlay, ColorHelper.Argb.fromFloats(1.0F, 1.0F, 1.0F, 1.0F));
        matrices.pop();
    }

    private void renderFluid(@Nullable FluidVariant fluid, double normalizedProgress,
                             VertexConsumerProvider vertexConsumers, MatrixStack matrices,
                             Vec3d start, Vec3d end) {

        if (fluid == null || fluid.isBlank()) return;
        Sprite stillSprite = FluidVariantRendering.getSprite(fluid);
        if (stillSprite == null) return;

        float maxFluidHeight = 64;
        RenderLayer renderLayer = RenderLayer.getItemEntityTranslucentCull(stillSprite.getAtlasId());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        matrices.push();

        matrices.pop();
    }

    private float getNormalizedLidOpenState(FermentBlockEntity blockEntity, float tickDelta) {
        float normalizedLidOpenState;
        if (blockEntity.isLidOpen()) normalizedLidOpenState = 1;
        else if (blockEntity.isLidClosed()) normalizedLidOpenState = 0;
        else {
            float interpolated;
            if (this.prevLidOpenTick < blockEntity.getLidOpeningTick()) {
                interpolated = blockEntity.getLidOpeningTick() + tickDelta;
                LoggerUtil.devLogger("Opens");
            } else if (this.prevLidOpenTick == blockEntity.getLidOpeningTick()) {
                interpolated = blockEntity.getLidOpeningTick();
            } else {
                interpolated = blockEntity.getLidOpeningTick() - tickDelta;
                LoggerUtil.devLogger("Closes");
            }
            LoggerUtil.devLogger(String.valueOf(interpolated));
            normalizedLidOpenState = MathHelper.clamp(interpolated, 0, FermentBlockEntity.LID_TOGGLE_DURATION) / FermentBlockEntity.LID_TOGGLE_DURATION;
        }
        return normalizedLidOpenState;
    }
}
