package net.shirojr.boatism.item.custom.upgrade;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismItems;

import java.util.List;

public class PerformanceFuelInjectorItem extends BoatismEquipmentItem {
    public PerformanceFuelInjectorItem(Settings settings) {
        super(settings);
    }

    @Override
    public List<BoatEngineComponent> getNecessaryParts() {
        return List.of(BoatismItems.COMPONENT_CANISTER_STRAPPED);
    }

    @Override
    public float addedThrust() {
        return 0.8f;
    }

    @Override
    public long addedConsumedFuel() {
        return FluidConstants.DROPLET * 20L;
    }

    @Override
    public MatrixStack itemRenderTransform(BoatEngineEntity boatEngineEntity, MatrixStack matrixStack) {
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        matrixStack.translate(0, 2.225, 0.225);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        return matrixStack;
    }
}
