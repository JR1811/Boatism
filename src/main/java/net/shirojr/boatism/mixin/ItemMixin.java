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
import net.shirojr.boatism.util.BoatEngineHandler;
import net.shirojr.boatism.util.LoggerUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    private void boatism$debugBoatCoupling(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand,
                                           CallbackInfoReturnable<ActionResult> cir) {
        World world = user.getWorld();
        Item usedItem = stack.getItem();
        if (world.isClient() || !(entity instanceof BoatEngineEntity boatEngineEntity)) return;

        if (usedItem.equals(Items.STICK)) {
            if (user.isSneaking()) {
                boatEngineEntity.setLocked(!boatEngineEntity.isLocked());
            }
            int boxSize = 5;
            Box box = new Box(user.getX() - boxSize, user.getY() - boxSize, user.getZ() - boxSize,
                    user.getX() + boxSize, user.getY() + boxSize, user.getZ() + boxSize);
            List<BoatEntity> boatList = world.getEntitiesByType(EntityType.BOAT, box, boatEntity -> true);
            if (boatList.size() > 0) {
                BoatEntity boat = boatList.get(0);
                boatEngineEntity.hookOntoBoatEntity(boat);

                LoggerUtil.devLogger(String.format("hooked engine to %s", boat.getName()));
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }

        BoatEngineHandler engineHandler = boatEngineEntity.getEngineHandler();

        if (usedItem.equals(Items.FLINT)) {
            engineHandler.fillUpFuel(BoatEngineHandler.MAX_FUEL);
        }
        if (usedItem.equals(Items.AMETHYST_SHARD)) {
            if (engineHandler.engineIsRunning()) {
                engineHandler.stopEngine();
            } else {
                engineHandler.startEngine();
            }
            LoggerUtil.devLogger(String.format("Engine is running: %s", engineHandler.engineIsRunning()));
        }
    }
}
