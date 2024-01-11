package net.shirojr.boatism.item.custom.upgrade;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

public class PlatesItem extends BoatismEquipmentItem {

    public PlatesItem(Settings settings) {
        super(settings);
    }

    @Override
    public float getAdditionalArmor() {
        return 5.0f;
    }

    @Override
    public MatrixStack itemRenderTransform(BoatEngineEntity boatEngineEntity, MatrixStack matrixStack) {
        matrixStack.push();
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        matrixStack.translate(0, 2.43, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        return matrixStack;
    }
}
