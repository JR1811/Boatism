package net.shirojr.boatism.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.util.NbtKeys;

public class FuelBucketItem extends Item /*BucketItem*/ {
    public final static float MAX_CAPACITY = 3000;

    public FuelBucketItem(/*Fluid fluid, */Settings settings) {
        super(/*fluid, */settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof BoatEngineEntity boatEngine)) return super.useOnEntity(stack, user, entity, hand);
        if (boatEngine.isRunning()) {
            user.sendMessage(Text.translatable("warning.boatism.engine_is_running"), true);
            return ActionResult.PASS;
        }

        if (user.getWorld().isClient()) return ActionResult.SUCCESS;

        if (!containsBucketFuelNbt(stack)) setFuelForItemStack(stack, MAX_CAPACITY);
        boatEngine.getEngineHandler().fillUpFuel(getFuelFromItemStack(stack));
        if (!user.isCreative()) {
            stack.decrement(1);
            user.getInventory().offerOrDrop(Items.BUCKET.getDefaultStack());
        }
        user.getWorld().playSound(null, boatEngine.getX(), boatEngine.getY(), boatEngine.getZ(),
                BoatismSounds.BOAT_ENGINE_FILL_UP, SoundCategory.NEUTRAL, 1f, 1f);
        return ActionResult.SUCCESS;
    }

    public static float getFuelFromItemStack(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(NbtKeys.BUCKET_FUEL)) return 0.0f;
        else return nbt.getFloat(NbtKeys.BUCKET_FUEL);
    }

    public static void setFuelForItemStack(ItemStack stack, float value) {
        stack.getOrCreateNbt().putFloat(NbtKeys.BUCKET_FUEL, value);
    }

    public static boolean containsBucketFuelNbt(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.contains(NbtKeys.BUCKET_FUEL);
    }
}
