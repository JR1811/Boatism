package net.shirojr.boatism.util.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record FluidStack(FluidVariant variant, long amount) {

    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(Registries.FLUID.getId(variant.getFluid()));
        buf.writeNbt(variant.getNbt());
        buf.writeLong(amount);
    }

    public static FluidStack read(PacketByteBuf buf) {
        Identifier id = buf.readIdentifier();
        Fluid fluid = Registries.FLUID.get(id);
        NbtCompound tag = buf.readNbt();
        long amount = buf.readLong();
        FluidVariant variant = FluidVariant.of(fluid, tag);
        return new FluidStack(variant, amount);
    }

    public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("fluid").forGetter(fs -> Registries.FLUID.getId(fs.variant().getFluid()).toString()),
            Codec.LONG.fieldOf("amount").forGetter(FluidStack::amount)
    ).apply(instance, (fluidIdStr, amount) -> {
        Fluid fluid = Registries.FLUID.get(new Identifier(fluidIdStr));
        return new FluidStack(FluidVariant.of(fluid, null), amount);
    }));
}
