package net.shirojr.boatism.util.data;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

public record EngineComponent(int slot, ItemStack componentStack) {

    public static EngineComponent read(PacketByteBuf buf) {
        int slot = buf.readVarInt();
        ItemStack stack = buf.readItemStack();
        return new EngineComponent(slot, stack);
    }

    public static void write(PacketByteBuf buf, EngineComponent value) {
        buf.writeVarInt(value.slot());
        buf.writeItemStack(value.componentStack());
    }
}