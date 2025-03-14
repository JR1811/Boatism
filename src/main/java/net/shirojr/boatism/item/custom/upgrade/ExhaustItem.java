package net.shirojr.boatism.item.custom.upgrade;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

public class ExhaustItem extends BoatismEquipmentItem {

    public ExhaustItem(Settings settings) {
        super(settings);
    }

    @Override
    public float addedOverheatTolerance() {
        return 2000.0f;
    }

    @Override
    public MatrixStack itemRenderTransform(BoatEngineEntity boatEngineEntity, MatrixStack matrixStack) {
        matrixStack.push();
        matrixStack.scale(0.82f, 0.82f, 0.82f);
        matrixStack.translate(0, 1.4, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        return matrixStack;
    }

    @Override
    public long addedConsumedFuel() {
        return 1;
    }
}
