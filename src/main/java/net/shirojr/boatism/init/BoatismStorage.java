package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.entity.custom.AirBoatEngineEntity;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

public class BoatismStorage {
    static {
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> InventoryStorage.of(blockEntity.getInventory(), null), BoatismBlockEntities.FERMENTER);
        BoatismApis.ENTITY_ITEM_STORAGE.registerForType((entity, direction) -> {
            if (!(entity instanceof BoatEngineEntity boatEngine)) return null;
            return boatEngine.getMountedInventoryStorage(direction);
        }, BoatismEntities.BOAT_ENGINE);
        BoatismApis.ENTITY_ITEM_STORAGE.registerForType((entity, direction) -> {
            if (!(entity instanceof AirBoatEngineEntity boatEngine)) return null;
            return boatEngine.getMountedInventoryStorage(direction);
        }, BoatismEntities.AIR_BOAT_ENGINE);
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.getInventory().getFluidStorage(), BoatismBlockEntities.FERMENTER);
        FluidStorage.ITEM.registerForItems((itemStack, context) -> {
            if (!itemStack.isOf(BoatismItems.FUEL_BUCKET)) return null;
            return new FullItemFluidStorage(context, itemVariant -> ItemVariant.of(Items.BUCKET, itemVariant.getComponents()), BoatismFluids.OIL.getFluidVariant(), FluidConstants.BUCKET);
        }, BoatismItems.FUEL_BUCKET);
    }

    public static void initialize() {
        // static initialisation
    }

    public static class BoatismApis {
        public static final EntityApiLookup<ItemStorage, Direction> ENTITY_ITEM_STORAGE =
                EntityApiLookup.get(Boatism.getId("entity_item_storage"), ItemStorage.class, Direction.class);
    }
}
