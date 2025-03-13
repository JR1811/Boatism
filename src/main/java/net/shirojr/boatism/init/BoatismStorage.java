package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Items;

public class BoatismStorage {
    static {
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> InventoryStorage.of(blockEntity.getInventory(), null), BoatismBlockEntities.FERMENTER);
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.getInventory().getFluidStorage(), BoatismBlockEntities.FERMENTER);
        FluidStorage.ITEM.registerForItems((itemStack, ctx) -> new SingleVariantStorage<>() {
            @Override
            protected FluidVariant getBlankVariant() {
                return FluidVariant.blank();
            }

            @Override
            protected long getCapacity(FluidVariant variant) {
                return FluidConstants.BUCKET;
            }

            @Override
            protected boolean canInsert(FluidVariant variant) {
                return ctx.getItemVariant().isOf(Items.BUCKET);
            }

            @Override
            protected boolean canExtract(FluidVariant variant) {
                return ctx.getItemVariant().isOf(BoatismItems.FUEL_BUCKET);
            }

            @Override
            protected void onFinalCommit() {
                if (getAmount() == 0) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        ctx.exchange(ItemVariant.of(Items.BUCKET), 1, transaction);
                        transaction.commit();
                    }
                } else {
                    try (Transaction transaction = Transaction.openOuter()) {
                        ctx.exchange(ItemVariant.of(BoatismItems.FUEL_BUCKET), 1, transaction);
                        transaction.commit();
                    }
                }
            }
        }, BoatismItems.FUEL_BUCKET, Items.BUCKET);
    }

    public static void initialize() {
        // static initialisation
    }
}
