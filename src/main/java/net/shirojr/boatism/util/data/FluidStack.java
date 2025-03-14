package net.shirojr.boatism.util.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record FluidStack(FluidVariant variant, long amount) {
    public static final PacketCodec<RegistryByteBuf, FluidStack> PACKET_CODEC = PacketCodec.tuple(
            FluidVariant.PACKET_CODEC, FluidStack::variant,
            PacketCodecs.VAR_LONG, FluidStack::amount,
            FluidStack::new
    );

    public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidVariant.CODEC.fieldOf("variant").forGetter(FluidStack::variant),
            Codec.LONG.fieldOf("amount").forGetter(FluidStack::amount)
    ).apply(instance, FluidStack::new));
}
