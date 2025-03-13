package net.shirojr.boatism.block.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class FermentBlockEntityModel extends Model {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ModelPart base, tank, mixer, holder, cap, sides, legs;

    public FermentBlockEntityModel(ModelPart root) {
        super(RenderLayer::getArmorCutoutNoCull);
        this.base = root.getChild("base");
        this.tank = this.base.getChild("tank");
        this.mixer = this.tank.getChild("mixer");
        this.holder = this.base.getChild("holder");
        this.cap = this.holder.getChild("cap");
        this.sides = this.holder.getChild("sides");
        this.legs = this.holder.getChild("legs");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData tank = base.addChild("tank", ModelPartBuilder.create().uv(14, 14).cuboid(-7.0F, -4.65F, 7.0F, 14.0F, 26.0F, 0.0F, new Dilation(0.0F))
                .uv(14, 14).cuboid(-7.0F, -4.65F, -7.0F, 14.0F, 26.0F, 0.0F, new Dilation(0.0F))
                .uv(14, 0).cuboid(7.0F, -4.65F, -7.0F, 0.0F, 26.0F, 14.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-7.0F, -4.65F, -7.0F, 0.0F, 26.0F, 14.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-7.0F, -5.65F, -7.0F, 14.0F, 1.0F, 14.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 8.0F, 0.0F));

        ModelPartData mixer = tank.addChild("mixer", ModelPartBuilder.create().uv(0, 53).cuboid(0.0F, -16.0F, -6.0F, 0.0F, 32.0F, 12.0F, new Dilation(0.0F))
                .uv(0, 65).cuboid(-6.0F, -16.0F, 0.0F, 12.0F, 32.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 10.0F, 0.0F));

        ModelPartData holder = base.addChild("holder", ModelPartBuilder.create().uv(0, 44).cuboid(-2.0F, -2.0F, -9.0F, 18.0F, 2.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(-7.0F, 2.0F, 0.0F));

        ModelPartData cap = holder.addChild("cap", ModelPartBuilder.create().uv(0, 44).cuboid(-9.0F, 0.0F, -2.0F, 18.0F, 2.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(7.0F, 28.0F, -7.0F));

        ModelPartData sides = holder.addChild("sides", ModelPartBuilder.create().uv(63, 19).cuboid(-1.0F, 0.0F, 0.25F, 2.0F, 29.0F, 1.0F, new Dilation(0.0F))
                .uv(56, 18).cuboid(-8.25F, 0.0F, -8.0F, 1.0F, 29.0F, 2.0F, new Dilation(0.0F))
                .uv(63, 19).cuboid(-1.0F, 0.0F, -15.25F, 2.0F, 29.0F, 1.0F, new Dilation(0.0F))
                .uv(56, 18).cuboid(7.25F, 0.0F, -8.0F, 1.0F, 29.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(7.0F, 0.0F, 7.0F));

        ModelPartData legs = holder.addChild("legs", ModelPartBuilder.create().uv(56, 0).cuboid(-1.0F, -14.0F, -17.0F, 2.0F, 16.0F, 2.0F, new Dilation(0.0F))
                .uv(56, 0).cuboid(-17.0F, -14.0F, -17.0F, 2.0F, 16.0F, 2.0F, new Dilation(0.0F))
                .uv(56, 0).cuboid(-17.0F, -14.0F, -1.0F, 2.0F, 16.0F, 2.0F, new Dilation(0.0F))
                .uv(56, 0).cuboid(-1.0F, -14.0F, -1.0F, 2.0F, 16.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(15.0F, -4.0F, 8.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.base.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    public void render(float normalizedLidState, float tickDelta, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.cap.pitch = (float) Math.toRadians(MathHelper.lerp(normalizedLidState, 0, -70));
        if (normalizedLidState != 0) this.mixer.visible = false;
        else this.mixer.visible = true;
        this.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
