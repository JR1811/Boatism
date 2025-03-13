package net.shirojr.boatism.util.storage;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.data.TransactionalLong;
import net.shirojr.boatism.util.tag.BoatismTags;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Contains a {@link SimpleInventory} and a fluid holding storage, implemented with Fabric API's {@link SingleFluidStorage}.
 *
 * @apiNote <ul>
 * <li>if the filter is not set (null) the fluid storage won't accept any fluids</li>
 * <li>if the filter is set to null or doesn't support the stored fluid anymore, the unsupported stored fluid will be cleared</li>
 * </ul>
 */
@SuppressWarnings("UnusedReturnValue")
public class FermentingInventory extends SimpleInventory {
    @Nullable
    private Predicate<FluidVariant> filter;
    private final SingleFluidStorage fluidStorage;
    private final InventoryStorage inventoryWrapper = InventoryStorage.of(this, null);

    public FermentingInventory(long fluidCapacity, @Nullable Predicate<FluidVariant> filter, int stacksSize) {
        super(stacksSize);
        this.filter = filter;
        this.fluidStorage = SingleFluidStorage.withFixedCapacity(fluidCapacity, this::markDirty);
    }

    public FermentingInventory(long fluidCapacity, @Nullable Predicate<FluidVariant> filter, ItemStack... stacks) {
        super(stacks);
        this.filter = filter;
        this.fluidStorage = SingleFluidStorage.withFixedCapacity(fluidCapacity, this::markDirty);
    }

    public FluidVariant getFluidVariant() {
        return this.getFluidStorage().getResource();
    }

    /**
     * @param filter
     * @apiNote If the filter is set to null or doesn't accept the currently stored fluid, the fluid will be removed
     */
    public void setFilter(@Nullable Predicate<FluidVariant> filter) {
        this.filter = filter;
        if (filter == null || !filter.test(this.getFluidStorage().getResource())) {
            this.clearFluidStorage();
        }
    }

    public boolean isFluidAllowed(FluidVariant fluid) {
        if (this.filter == null) {
            LoggerUtil.LOGGER.error("Fermenting Inventory filter wasn't set");
            return false;
        }
        return filter.test(fluid);
    }

    public boolean hasFluidStorageEnoughSpace(int amount) {
        return this.getFluidStorage().getAmount() + amount <= this.getFluidStorage().getCapacity();
    }

    public SingleFluidStorage getFluidStorage() {
        return fluidStorage;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (stack.isIn(BoatismTags.Items.FERMENTABLE) && super.canInsert(stack)) {
            return true;
        }
        ContainerItemContext context = ContainerItemContext.withConstant(stack);
        Storage<FluidVariant> itemFluidStorage = FluidStorage.ITEM.find(stack, context);
        if (itemFluidStorage == null) return false;
        if (!this.getFluidStorage().supportsInsertion()) return false;
        if (this.getFluidStorage().getAmount() >= this.getFluidStorage().getCapacity()) return false;

        for (var fluidEntry : itemFluidStorage) {
            if (fluidEntry.isResourceBlank()) continue;
            FluidVariant fluid = fluidEntry.getResource();
            if (this.isFluidAllowed(fluid)) {
                if (this.getFluidStorage().getAmount() + fluidEntry.getAmount() <= this.getFluidStorage().getCapacity()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Inserts ItemStack into the fitting storage. Fluid containing ItemStacks will fill their content into the FluidStorage
     * and otherwise allowed ItemStacks will be put into the inventory.
     *
     * @return true, if the ItemStack (or its content) was inserted successfully
     */
    public boolean insert(ItemStack stack) {
        if (!canInsert(stack)) return false;
        ContainerItemContext context = ContainerItemContext.withConstant(stack);
        Storage<FluidVariant> itemFluidStorage = FluidStorage.ITEM.find(stack, context);
        if (itemFluidStorage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                long movedAmount = StorageUtil.move(
                        itemFluidStorage,
                        this.getFluidStorage(),
                        this::isFluidAllowed,
                        FluidConstants.BUCKET,
                        transaction
                );
                if (movedAmount == FluidConstants.BUCKET) {
                    transaction.commit();
                    return true;
                }
            }
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long insertedAmount = this.inventoryWrapper.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            if (insertedAmount == stack.getCount()) {
                transaction.commit();
                return true;
            }
        }
        return false;
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    /**
     * This modifies the stored fluid amount. To check if the fluid storage has enough space beforehand,
     * use {@link FermentingInventory#hasFluidStorageEnoughSpace(int) hasFluidStorageEnoughSpace()}
     *
     * @return {@link Optional#empty()} if the insertion failed completely or > 0 if fluid storage didn't have enough space for this operation.
     * In this case the fluid storage will be filled and this returns the
     * leftover amount of fluid, which wasn't inserted into the fluid storage.
     */
    public Optional<Long> insertFluid(FluidVariant fluid, long amount) {
        if (fluid.isOf(FluidVariant.blank().getFluid())) {
            clearFluidStorage();
            return Optional.of(0L);
        }
        TransactionalLong overflow = new TransactionalLong(0);
        try (Transaction transaction = Transaction.openOuter()) {
            if (!this.isFluidAllowed(fluid)) transaction.abort();
            long insertedAmount = this.getFluidStorage().insert(fluid, amount, transaction);
            overflow.set(transaction, Math.max(0, amount - insertedAmount));
            transaction.commit();
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
        return Optional.of(overflow.get());
    }

    /**
     * This modifies the stored fluid amount. To check which fluid was stored,
     * use {@link FermentingInventory#getFluidVariant() getFluidVariant()}
     *
     * @return > 0 if fluid storage didn't have enough fluid stored for this operation.
     * This represents the missing amount of the requested fluid
     */
    public long extractFluid(long amount) {
        TransactionalLong underflow = new TransactionalLong(0);
        try (Transaction transaction = Transaction.openOuter()) {
            long extractedAmount = this.getFluidStorage().extract(this.getFluidVariant(), amount, transaction);
            underflow.set(transaction, Math.max(0, amount - extractedAmount));
            transaction.commit();
        }
        return underflow.get();
    }

    public void clearFluidStorage() {
        FluidVariant fluidVariant = this.fluidStorage.getResource();
        if (fluidVariant == null || this.getFluidStorage().isResourceBlank()) return;
        try (Transaction transaction = Transaction.openOuter()) {
            this.getFluidStorage().extract(fluidVariant, this.getFluidStorage().getAmount(), transaction);
            transaction.commit();
        }
    }

    public void clearItemsAndFluid() {
        super.clear();
        clearFluidStorage();
        markDirty();
    }

    @Override
    public boolean isEmpty() {
        if (!super.isEmpty()) return false;
        return this.getFluidStorage().getAmount() <= 0;
    }

    @Override
    public String toString() {
        return super.toString() + this.getFluidStorage().getResource().toString() +
                this.getFluidStorage().getAmount() + "/" + this.getFluidStorage().getCapacity();
    }

    /**
     * Reads the stored Items and the FluidVariant
     */
    @Override
    public void readNbtList(NbtList nbtList) {
        this.clearItemsAndFluid();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtEntry = nbtList.getCompound(i);
            ItemStack itemStack = ItemStack.fromNbt(nbtEntry);
            if (!itemStack.isEmpty()) {
                this.addStack(itemStack);
            } else if (nbtEntry.contains("variant")) {
                FluidVariant variant = FluidVariant.fromNbt(nbtEntry.getCompound("variant"));
                long amount = nbtEntry.getLong("amount");
                this.setFilter(getSimpleFluidPredicate(variant));
                this.insertFluid(variant, amount);
            }
        }
    }

    /**
     * Writes the stored Items and the FluidVariant
     */
    @Override
    public NbtList toNbtList() {
        NbtList list = super.toNbtList();
        NbtCompound fluidNbt = new NbtCompound();
        this.getFluidStorage().writeNbt(fluidNbt);
        list.add(fluidNbt);
        return list;
    }

    public static Predicate<FluidVariant> getSimpleFluidPredicate(FluidVariant... allowedFluid) {
        return fluidVariant -> Arrays.asList(allowedFluid).contains(fluidVariant);
    }
}
