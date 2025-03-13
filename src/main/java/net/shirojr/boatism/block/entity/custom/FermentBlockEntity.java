package net.shirojr.boatism.block.entity.custom;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.boatism.block.custom.FermentBlock;
import net.shirojr.boatism.init.BoatismBlockEntities;
import net.shirojr.boatism.init.BoatismFluids;
import net.shirojr.boatism.util.storage.FermentingInventory;
import net.shirojr.boatism.util.tag.BoatismTags;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class FermentBlockEntity extends BlockEntity {
    public static final int LID_TOGGLE_DURATION = 5, HEAT_UP_DURATION = 100, MIXING_DURATION = 200;
    public static final long FLUID_CAPACITY = FluidConstants.BUCKET * 5;
    public static final int STACKS_CAPACITY = 4;
    public static final FluidVariant INPUT_FLUID = FluidVariant.of(Fluids.WATER);

    private int lidOpeningTick, heatTick, mixingTick;
    private final FermentingInventory inventory = new FermentingInventory(
            FLUID_CAPACITY,
            FermentingInventory.getSimpleFluidPredicate(
                    FluidVariant.of(Fluids.WATER),
                    FluidVariant.of(BoatismFluids.OIL.still())
            ),
            STACKS_CAPACITY
    );

    public FermentBlockEntity(BlockPos pos, BlockState state) {
        super(BoatismBlockEntities.FERMENTER, pos, state);
    }

    /**
     * @apiNote Only use to access information<br>
     * Modifying the inventory instance with this method will avoid storing changed information to the chunk
     * and potentially created discrepancies between the logical server and client side.
     * @see FermentBlockEntity#modifyInventory(BiConsumer) modifyInventory
     */
    public FermentingInventory getInventory() {
        return inventory;
    }

    /**
     * Provides the instance of the BlockEntity's inventory. This will store changes to the chunk if it is
     * executed on the logical server side. This is used for syncing changes to the client side.
     *
     * @see FermentBlockEntity#getInventory() getInventory
     */
    public void modifyInventory(BiConsumer<FermentingInventory, World> inventoryProvider) {
        inventoryProvider.accept(this.inventory, this.getWorld());
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(this.getPos());
            markDirty();
        }
    }

    public int getLidOpeningTick() {
        return lidOpeningTick;
    }

    public void setLidOpeningTick(int lidOpeningTick) {
        this.lidOpeningTick = lidOpeningTick;
        if (this.getLidOpeningTick() >= LID_TOGGLE_DURATION || this.getLidOpeningTick() <= 0) {
            markDirty();
        }
    }

    public boolean isLidOpen() {
        return this.getCachedState().get(FermentBlock.OPEN) && this.getLidOpeningTick() >= LID_TOGGLE_DURATION;
    }

    public boolean isLidClosed() {
        return !this.getCachedState().get(FermentBlock.OPEN) && this.getLidOpeningTick() <= 0;
    }

    public int getHeatTick() {
        return heatTick;
    }

    public void setHeatTick(int heatTick) {
        this.heatTick = heatTick;
        if (this.getHeatTick() >= HEAT_UP_DURATION || this.getHeatTick() <= 0) {
            markDirty();
        }
    }

    public boolean isReceivingHeat() {
        if (this.world == null) return false;
        BlockState stateBelow = this.world.getBlockState(this.pos.down());
        if (!(stateBelow.contains(Properties.LIT))) return false;
        return stateBelow.get(Properties.LIT);
    }

    public boolean canCaptureHeat() {
        return getLidOpeningTick() <= 0;
    }

    public boolean isFullyHeated() {
        return this.getHeatTick() >= HEAT_UP_DURATION;
    }

    public int getMixingTick() {
        return mixingTick;
    }

    public void setMixingTick(int mixingTick) {
        this.mixingTick = mixingTick;
        if (this.getMixingTick() >= MIXING_DURATION || this.getMixingTick() <= 0) {
            markDirty();
        }
    }

    public void addMixingTick(int value) {
        this.setMixingTick(MathHelper.clamp(this.getMixingTick() + value, 0, MIXING_DURATION));
    }

    public boolean isValidFluidStack(ItemStack stack) {
        if (stack.isEmpty()) return true;
        Storage<FluidVariant> storage = ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
        if (storage == null) return false;
        for (var entry : storage) {
            if (!entry.getResource().equals(INPUT_FLUID) && !entry.isResourceBlank()) return false;
        }
        return storage.supportsInsertion();
    }

    public boolean canAcceptFluid(FluidVariant fluid, long amount) {
        SingleFluidStorage fluidStorage = this.getInventory().getFluidStorage();
        return isLidOpen() && fluidStorage.getAmount() < fluidStorage.getCapacity();
    }

    public boolean canAcceptFluid(ItemStack stack) {
        if (!isValidFluidStack(stack)) return false;
        SingleFluidStorage fluidStorage = this.getInventory().getFluidStorage();
        return isLidOpen() && fluidStorage.getAmount() < fluidStorage.getCapacity();
    }

    public boolean hasAllIngredients() {
        if (!this.getInventory().getFluidVariant().equals(INPUT_FLUID)) return false;
        for (ItemStack stack : this.getInventory().getHeldStacks()) {
            if (stack.isEmpty()) return false;
            if (!stack.isIn(BoatismTags.Items.FERMENTABLE)) return false;
        }
        return true;
    }

    public boolean finishedFermenting() {
        return this.getMixingTick() >= MIXING_DURATION;
    }

    private void finishFermenting() {
        this.modifyInventory((fermentingInventory, world) -> {
            long amount = fermentingInventory.getFluidStorage().getAmount();
            fermentingInventory.clearItemsAndFluid();
            fermentingInventory.insertFluid(FluidVariant.of(BoatismFluids.OIL.still()), amount);
        });
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            FermentBlock.toggleOpenedState(serverWorld, this.getPos());
        }
    }

    /**
     * Extracts precisely one Bucket of either water or oil fluid from the BlockEntity's fluid storage
     * and fills up the ItemStack if it was a fitting canister.
     *
     * @return true, if storage got the fluid extracted and moved over into the canister ItemStack successfully.
     */
    public boolean extractAndFillCanister(PlayerEntity player, Hand hand) {
        ContainerItemContext handContext = ContainerItemContext.ofPlayerHand(player, hand);
        Storage<FluidVariant> itemFluidStorage = handContext.find(FluidStorage.ITEM);
        if (itemFluidStorage == null) return false;
        try (Transaction transaction = Transaction.openOuter()) {
            Predicate<FluidVariant> filter = variant -> variant.isOf(Fluids.WATER) || variant.isOf(BoatismFluids.OIL.still());
            long fluidMoved = StorageUtil.move(this.getInventory().getFluidStorage(), itemFluidStorage, filter, FluidConstants.BUCKET, transaction);
            if (fluidMoved == FluidConstants.BUCKET) {
                transaction.commit();
            } else {
                transaction.abort();
                return false;
            }
        }
        playSound(player.getWorld(), SoundEvents.ITEM_BUCKET_FILL);
        return true;
    }

    /**
     * Inserts precisely one Bucket of either water or oil fluid from the ItemStack's fluid storage
     * and fills up the BlockEntity Fluid Storage if it allowed for insertion.
     *
     * @return true, if canister got the fluid extracted and moved over into the BlockEntity's Fluid Storage successfully.
     */
    public boolean insertAndEmptyCanister(PlayerEntity player, Hand hand) {
        ContainerItemContext handContext = ContainerItemContext.ofPlayerHand(player, hand);
        Storage<FluidVariant> itemFluidStorage = handContext.find(FluidStorage.ITEM);
        if (itemFluidStorage == null) return false;
        try (Transaction transaction = Transaction.openOuter()) {
            Predicate<FluidVariant> filter = variant -> variant.isOf(Fluids.WATER) || variant.isOf(BoatismFluids.OIL.still());
            //TODO: fluid storage predicate check?
            long fluidMoved = StorageUtil.move(itemFluidStorage, this.getInventory().getFluidStorage(), filter, FluidConstants.BUCKET, transaction);
            if (fluidMoved == FluidConstants.BUCKET) {
                transaction.commit();
            } else {
                transaction.abort();
                return false;
            }
        }
        playSound(player.getWorld(), SoundEvents.ITEM_BUCKET_EMPTY);
        return true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, FermentBlockEntity blockEntity) {
        if (world == null) return;

        if (state.get(FermentBlock.OPEN) && blockEntity.getLidOpeningTick() < FermentBlockEntity.LID_TOGGLE_DURATION) {
            blockEntity.setLidOpeningTick(blockEntity.getLidOpeningTick() + 1);
            if (blockEntity.isLidOpen()) {
                blockEntity.playSound(world, SoundEvents.BLOCK_COPPER_DOOR_OPEN);
            }
        } else if (!state.get(FermentBlock.OPEN) && blockEntity.getLidOpeningTick() > 0) {
            blockEntity.setLidOpeningTick(blockEntity.getLidOpeningTick() - 1);
            if (blockEntity.isLidClosed()) {
                blockEntity.playSound(world, SoundEvents.BLOCK_COPPER_DOOR_CLOSE);
            }
        }

        if (blockEntity.isReceivingHeat() && blockEntity.canCaptureHeat()) {
            if (!blockEntity.isFullyHeated()) {
                blockEntity.setHeatTick(blockEntity.getHeatTick() + 1);
            }
        } else if (blockEntity.getHeatTick() > 0) {
            blockEntity.setHeatTick(blockEntity.getHeatTick() - 1);
        }

        if (blockEntity.isFullyHeated() && blockEntity.hasAllIngredients()) {
            blockEntity.addMixingTick(1);
        } else if (blockEntity.getHeatTick() > 0) {
            blockEntity.addMixingTick(-1);
        }

        if (blockEntity.finishedFermenting()) {
            blockEntity.finishFermenting();
        }
    }

    private void playSound(World world, SoundEvent soundEvent) {
        if (world.isClient()) return;
        world.playSound(null, this.getPos(), soundEvent, SoundCategory.BLOCKS, 2f, 1f);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return createNbt();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("inventory")) {
            NbtList list = nbt.getList("inventory", NbtElement.COMPOUND_TYPE);
            this.getInventory().readNbtList(list);
        }
        this.setLidOpeningTick(nbt.getInt("lidOpeningTick"));
        this.setHeatTick(nbt.getInt("heatTick"));
        this.setMixingTick(nbt.getInt("mixingTick"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList list = this.getInventory().toNbtList();
        nbt.put("inventory", list);
        nbt.putInt("lidOpeningTick", this.getLidOpeningTick());
        nbt.putInt("heatTick", this.getHeatTick());
        nbt.putInt("mixingTick", this.getMixingTick());
    }
}
