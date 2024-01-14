package net.shirojr.boatism.entity.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

public class EquipedPartFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends FeatureRenderer<T, M> {
    private final ItemRenderer itemRenderer;

    public EquipedPartFeatureRenderer(FeatureRendererContext<T, M> context, ItemRenderer itemRenderer) {
        super(context);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        if (!(entity instanceof BoatEngineEntity boatEngine)) return;
        for (ItemStack stack : boatEngine.getMountedInventory().getHeldStacks()) {
            if (stack.isEmpty() || !(stack.getItem() instanceof BoatEngineComponent component)) continue;
            ItemStack displayedStack = component.getMountedItemStack(stack);
            matrices = component.itemRenderTransform(boatEngine, matrices);
            this.renderItem(displayedStack, light, matrices, vertexConsumers,
                    boatEngine.getWorld(), boatEngine.getId());
            matrices.pop();
        }
    }

    private void renderItem(ItemStack stack, int light, MatrixStack matrices,
                            VertexConsumerProvider vertexConsumer, World world, int seed) {
        itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV,
                matrices, vertexConsumer, world, seed);
    }
}
