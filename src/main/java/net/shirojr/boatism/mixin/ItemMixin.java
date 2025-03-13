package net.shirojr.boatism.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismItems;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.handler.BoatEngineHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "useOnEntity", at = @At(value = "HEAD"), cancellable = true)
    private void boatism$debugBoatCoupling(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand,
                                           CallbackInfoReturnable<ActionResult> cir) {
        World world = entity.getWorld();
        Item usedItem = stack.getItem();
        if (!(entity instanceof BoatEngineEntity boatEngineEntity)) return;
        BoatEngineHandler engineHandler = boatEngineEntity.getEngineHandler();

        if (world.isClient()) {
            return;
        }
        if (usedItem.equals(Items.STICK) && user.isSneaking()) {
            cir.setReturnValue(boatism$engineCoupling(user, boatEngineEntity, world));
            return;
        }
        if (usedItem.equals(BoatismItems.FUEL_BUCKET)) {
            float leftOver = engineHandler.fillUpFuel(BoatEngineHandler.MAX_BASE_FUEL);
            if (!user.isCreative()) {
                stack.decrement(1);
                user.getInventory().offerOrDrop(Items.BUCKET.getDefaultStack());
            }
            LoggerUtil.devLogger(String.format("Filled up fuel. %s was left over", leftOver));
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Unique
    private ActionResult boatism$engineCoupling(PlayerEntity user, BoatEngineEntity boatEngineEntity, World world) {
        if (user.isSneaking()) {
            boatEngineEntity.setLocked(!boatEngineEntity.isLocked());
            return ActionResult.PASS;
        }
        int boxSize = 5;
        Box box = new Box(user.getX() - boxSize, user.getY() - boxSize, user.getZ() - boxSize,
                user.getX() + boxSize, user.getY() + boxSize, user.getZ() + boxSize);
        List<BoatEntity> boatList = world.getEntitiesByType(EntityType.BOAT, box, boatEntity -> true);
        boatList.addAll(world.getEntitiesByType(EntityType.CHEST_BOAT, box, chestBoatEntity -> true));
        if (boatList.size() > 0) {
            BoatEntity boat = boatList.get(0);
            boatEngineEntity.hookOntoBoatEntity(boat);

            LoggerUtil.devLogger(String.format("hooked engine to %s", boat.getName()));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
