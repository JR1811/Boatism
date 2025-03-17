package net.shirojr.boatism.item.custom;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismFluids;
import net.shirojr.boatism.init.BoatismSounds;
import org.jetbrains.annotations.Nullable;

public class FuelBucketItem extends BucketItem {
    public final static long MAX_CAPACITY = FluidConstants.BUCKET;

    public FuelBucketItem(Fluid fluid, Settings settings) {
        super(fluid, settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof BoatEngineEntity boatEngine)) return super.useOnEntity(stack, user, entity, hand);
        if (boatEngine.isRunning()) {
            user.sendMessage(Text.translatable("warning.boatism.engine_is_running"), true);
            return ActionResult.PASS;
        }

        try (Transaction transaction = Transaction.openOuter()) {
            var fluidStorage = getFluidStorage(user, hand);
            if (fluidStorage == null) {
                transaction.abort();
                return ActionResult.PASS;
            }
            long extractedAmount = fluidStorage.extract(BoatismFluids.OIL.getFluidVariant(), MAX_CAPACITY, transaction);
            boatEngine.getEngineHandler().fillUpFuel(extractedAmount);
        }
        user.getWorld().playSound(null, boatEngine.getBlockPos(), BoatismSounds.BOAT_ENGINE_FILL_UP, SoundCategory.NEUTRAL, 1f, 1f);
        return ActionResult.SUCCESS;
    }

    @Nullable
    public static Storage<FluidVariant> getFluidStorage(PlayerEntity player, Hand hand) {
        ContainerItemContext context = ContainerItemContext.ofPlayerHand(player, hand);
        ItemStack stack = player.getStackInHand(hand);
        return FluidStorage.ITEM.find(stack, context);
    }
}
