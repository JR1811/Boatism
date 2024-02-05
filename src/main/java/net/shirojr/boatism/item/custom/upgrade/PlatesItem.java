package net.shirojr.boatism.item.custom.upgrade;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.item.BoatismItems;

import java.util.List;

public class PlatesItem extends BoatismEquipmentItem {

    public PlatesItem(Settings settings) {
        super(settings);
    }

    @Override
    public List<Item> getConflictingParts() {
        return List.of(BoatismItems.COMPONENT_CANISTER, BoatismItems.COMPONENT_CANISTER_STRAPPED);
    }

    @Override
    public float getAdditionalArmor() {
        return 20.0f;
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

    @Override
    public boolean waterProofsEngine() {
        return true;    //TODO: find a better item to waterproof engine
    }
}
