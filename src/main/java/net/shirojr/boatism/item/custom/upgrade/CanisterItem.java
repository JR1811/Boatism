package net.shirojr.boatism.item.custom.upgrade;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismDataComponents;
import net.shirojr.boatism.init.BoatismItems;

import java.util.List;

public class CanisterItem extends BoatismEquipmentItem implements BoatEngineComponent {

    public CanisterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getMountedItemStack(ItemStack originalStack) {
        ItemStack output = new ItemStack(BoatismItems.COMPONENT_CANISTER_STRAPPED);
        output.set(BoatismDataComponents.ORIGINAL_ITEM, originalStack);
        return output;
    }

    @Override
    public ItemStack getReturnedItemStack(ItemStack displayedItemStack) {
        ItemStack output = new ItemStack(BoatismItems.COMPONENT_CANISTER);
        output.set(BoatismDataComponents.DISPLAYED_ITEM, displayedItemStack);
        return output;
    }

    @Override
    public MatrixStack itemRenderTransform(BoatEngineEntity boatEngineEntity, MatrixStack matrixStack) {
        matrixStack.push();
        float scaleFactor = 0.48f;
        matrixStack.scale(scaleFactor, scaleFactor, scaleFactor);
        matrixStack.translate(0.05, 2.3, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        return matrixStack;
    }

    @Override
    public List<Item> getConflictingParts() {
        return List.of(BoatismItems.COMPONENT_PLATES);
    }

    @Override
    public long addedFuelCapacity() {
        return FluidConstants.BUCKET * 5;
    }

    @Override
    public long addedConsumedFuel() {
        return 1;
    }
}
