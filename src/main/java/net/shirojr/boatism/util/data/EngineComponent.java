package net.shirojr.boatism.util.data;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record EngineComponent(int slot, ItemStack componentStack) {
    public static final PacketCodec<RegistryByteBuf, EngineComponent> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, EngineComponent::slot,
            ItemStack.PACKET_CODEC, EngineComponent::componentStack,
            EngineComponent::new
    );
}
