package net.shirojr.boatism.screen.handler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.shirojr.boatism.api.BoatEngineComponent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismScreenHandlers;
import net.shirojr.boatism.network.packet.OpenEngineInventoryPacket;

public class EngineControlScreenHandler extends ScreenHandler {
    private final Inventory engineInventory;
    private final PropertyDelegate delegate;
    private BoatEngineEntity boatEngine;

    public EngineControlScreenHandler(int syncId, PlayerInventory playerInventory, OpenEngineInventoryPacket openEngineInventoryPacket) {
        this(syncId, playerInventory, new SimpleInventory(12), new ArrayPropertyDelegate(6), openEngineInventoryPacket);
    }

    public EngineControlScreenHandler(int syncId, PlayerInventory playerInventory, Inventory engineInventory,
                                      PropertyDelegate delegate, OpenEngineInventoryPacket data) {
        super(BoatismScreenHandlers.ENGINE_CONTROL_SCREEN_HANDLER, syncId);
        checkSize(engineInventory, 12);
        this.engineInventory = engineInventory;
        this.delegate = delegate;
        PlayerEntity player = playerInventory.player;
        if (!player.getWorld().isClient()) {
            if (player.getWorld().getEntityById(data.entityNetworkId()) instanceof BoatEngineEntity entity) {
                this.boatEngine = entity;
            }
        }

        addPlayerHotbar(playerInventory);
        addPlayerInventory(playerInventory);
        addEngineInventorySlots(engineInventory);

        addProperties(delegate);
    }

    public boolean isEngineRunning() {
        return this.delegate.get(0) == 1;
    }

    public int getPowerLevel() {
        return this.delegate.get(1);
    }

    public float getFuel() {
        return (float) this.delegate.get(2) / 100;
    }

    public float getMaxFuel() {
        return (float) this.delegate.get(3) / 100;
    }

    public float getOverheat() {
        return (float) this.delegate.get(4) / 100;
    }

    public float getMaxOverheat() {
        return (float) this.delegate.get(5) / 100;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getStack();
        ItemStack newStack = originalStack.copy();
        if (slotIndex < this.engineInventory.size()) {
            if (!this.insertItem(originalStack, this.engineInventory.size(), this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.insertItem(originalStack, 0, this.engineInventory.size(), false)) {
            return ItemStack.EMPTY;
        }
        if (originalStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        return newStack;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (this.boatEngine != null && player.getWorld().isClient()) {
            this.boatEngine.syncComponentListToTrackingClients();
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.engineInventory.canPlayerUse(player);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        boolean isEngineComponent = stack.getItem() instanceof BoatEngineComponent;
        return switch (slot.getIndex()) {
            case 0, 1, 2 -> isEngineComponent;
            // TODO: include fuel slot handling!
            default -> true;
        };
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private void addEngineInventorySlots(Inventory engineInventory) {
        int maxRows = 4, maxColumns = 3, index = 0;
        for (int row = 0; row < maxRows; row++) {
            for (int column = 0; column < maxColumns; column++) {
                this.addSlot(new Slot(engineInventory, index, 116 + column * 18, 7 + row * 18));
                index++;
            }
        }
    }
}
