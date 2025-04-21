package net.shirojr.boatism.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.shirojr.boatism.entity.custom.AirBoatEngineEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class AirBoatEngineEntityModel<T extends AirBoatEngineEntity> extends SinglePartEntityModel<T> {
    private final ModelPart base, cage, wallsSide, wallsCover, casing, rotor, blades, engine, straps;
    private final List<ModelPart> parts = new ArrayList<>();

    public AirBoatEngineEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.base = root.getChild("base");
        this.cage = base.getChild("cage");
        this.wallsSide = cage.getChild("wallsSide");
        this.wallsCover = cage.getChild("wallsCover");
        this.casing = base.getChild("casing");
        this.rotor = base.getChild("rotor");
        this.blades = rotor.getChild("blades");
        this.engine = base.getChild("engine");
        this.straps = base.getChild("straps");
        parts.addAll(List.of(this.base, this.cage, this.wallsSide, this.wallsCover, this.casing, this.blades, this.engine, this.straps));
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData cage = base.addChild("cage", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, -4.0F));

        ModelPartData wallsSide = cage.addChild("wallsSide", ModelPartBuilder.create().uv(32, 0).cuboid(-8.0F, 0.0F, 6.0F, 16.0F, 0.0F, 3.0F, new Dilation(0.0F))
                .uv(24, 35).cuboid(-8.0F, -16.0F, 6.0F, 0.0F, 16.0F, 3.0F, new Dilation(0.0F))
                .uv(30, 35).cuboid(8.0F, -16.0F, 6.0F, 0.0F, 16.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 32).cuboid(-8.0F, -16.0F, 6.0F, 16.0F, 0.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData wallsCover = cage.addChild("wallsCover", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -16.0F, 9.0F, 16.0F, 16.0F, 0.0F, new Dilation(0.0F))
                .uv(0, 16).cuboid(-8.0F, -16.0F, 6.0F, 16.0F, 16.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData casing = base.addChild("casing", ModelPartBuilder.create().uv(32, 24).cuboid(-2.0F, -9.0F, -1.0F, 4.0F, 3.0F, 5.0F, new Dilation(0.0F))
                .uv(36, 43).cuboid(-2.0F, -10.0F, 4.9F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 45).cuboid(-2.0F, -10.0F, 9.1F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(16, 37).cuboid(-1.0F, -6.0F, 4.9F, 2.0F, 6.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 32).cuboid(-4.5F, -8.5F, 0.0F, 9.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(44, 48).cuboid(3.5F, -8.5F, 0.1F, 1.0F, 7.0F, 0.0F, new Dilation(0.0F))
                .uv(16, 50).cuboid(-4.5F, -8.5F, 0.9F, 1.0F, 7.0F, 0.0F, new Dilation(0.0F))
                .uv(14, 50).cuboid(-4.5F, -8.5F, 0.1F, 1.0F, 7.0F, 0.0F, new Dilation(0.0F))
                .uv(12, 50).cuboid(3.5F, -8.5F, 0.9F, 1.0F, 7.0F, 0.0F, new Dilation(0.0F))
                .uv(10, 45).cuboid(-1.0F, -7.0F, 1.9F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, -4.0F));

        ModelPartData cube_r1 = casing.addChild("cube_r1", ModelPartBuilder.create().uv(16, 37).cuboid(-1.0F, -1.5F, -0.5F, 2.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -0.75F, 3.4F, -1.5708F, 0.0F, -3.1416F));

        ModelPartData rotor = base.addChild("rotor", ModelPartBuilder.create().uv(32, 3).cuboid(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -8.0F, 2.0F));

        ModelPartData blades = rotor.addChild("blades", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 1.5F));

        ModelPartData cube_r2 = blades.addChild("cube_r2", ModelPartBuilder.create().uv(6, 50).cuboid(-1.5F, -7.5F, 0.0F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 1.5708F));

        ModelPartData cube_r3 = blades.addChild("cube_r3", ModelPartBuilder.create().uv(0, 50).cuboid(-1.5F, -7.5F, 0.0F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 3.1416F));

        ModelPartData cube_r4 = blades.addChild("cube_r4", ModelPartBuilder.create().uv(36, 48).cuboid(-1.5F, -7.5F, 0.0F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, -1.5708F));

        ModelPartData cube_r5 = blades.addChild("cube_r5", ModelPartBuilder.create().uv(46, 43).cuboid(-1.5F, -7.5F, 0.0F, 3.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData engine = base.addChild("engine", ModelPartBuilder.create().uv(32, 14).cuboid(-2.0F, -1.1304F, -3.5F, 4.0F, 3.0F, 7.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -1.8696F, -3.5F, 0.0F, -1.5708F, 0.0F));

        ModelPartData cube_r6 = engine.addChild("cube_r6", ModelPartBuilder.create().uv(0, 35).cuboid(-5.5F, -0.5F, -0.5F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -0.1304F, 0.0F, 0.0F, 1.5708F, 0.0F));

        ModelPartData cube_r7 = engine.addChild("cube_r7", ModelPartBuilder.create().uv(0, 37).cuboid(-1.0F, -4.0F, -6.0F, 3.0F, 3.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.8696F, 3.5F, 0.0F, 0.0F, 0.4363F));

        ModelPartData cube_r8 = engine.addChild("cube_r8", ModelPartBuilder.create().uv(0, 37).cuboid(-1.5F, -1.5F, -2.5F, 3.0F, 3.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(-1.5097F, -0.1848F, 0.0F, -3.1416F, 0.0F, 2.7053F));

        ModelPartData straps = base.addChild("straps", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, -1.0F));

        ModelPartData cube_r9 = straps.addChild("cube_r9", ModelPartBuilder.create().uv(22, 45).cuboid(-2.0F, 0.0F, 0.0F, 1.0F, 7.0F, 0.0F, new Dilation(0.0F))
                .uv(42, 48).cuboid(1.0F, 0.0F, 0.0F, 1.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -6.0F, -1.0F, 0.6109F, 0.0F, 0.0F));

        ModelPartData cube_r10 = straps.addChild("cube_r10", ModelPartBuilder.create().uv(20, 44).cuboid(-2.0F, 0.0F, 0.0F, 1.0F, 8.0F, 0.0F, new Dilation(0.0F))
                .uv(22, 37).cuboid(1.0F, 0.0F, 0.0F, 1.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -16.0F, 3.0F, -0.48F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(AirBoatEngineEntity entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        this.base.traverse().forEach(ModelPart::resetTransform);
        float baseSpinSpeed = 3f;
        float powerLevelMultiplier = entity.getPowerLevel() * 0.2f;
        this.rotor.roll = animationProgress;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        float scaleFactor = 1.0f;

        matrices.translate(0.0f, 0.0f, 0.1f);
        matrices.scale(scaleFactor, scaleFactor, scaleFactor);
        base.render(matrices, vertices, light, overlay, color);
    }

    @Override
    public ModelPart getPart() {
        return this.base;
    }
}
