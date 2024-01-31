package net.shirojr.boatism.item.custom.upgrade;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.item.BoatismItems;

import java.util.List;

public class CanisterItem extends BoatismEquipmentItem implements BoatEngineComponent {

    public CanisterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getMountedItemStack(ItemStack originalStack) {
        ItemStack output = new ItemStack(BoatismItems.COMPONENT_CANISTER_STRAPPED);
        output.setNbt(originalStack.getNbt());
        return output;
    }

    @Override
    public ItemStack getReturnedItemStack(ItemStack displayedStack) {
        ItemStack output = new ItemStack(BoatismItems.COMPONENT_CANISTER);
        output.setNbt(displayedStack.getNbt());
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
    public float addedFuelCapacity() {
        return 5500.0f;
    }

    @Override
    public float addedConsumedFuel() {
        return 0.2f;
    }
}
