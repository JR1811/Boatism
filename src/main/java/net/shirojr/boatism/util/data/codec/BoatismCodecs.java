package net.shirojr.boatism.util.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.EulerAngle;

import java.util.LinkedHashSet;
import java.util.List;

public class BoatismCodecs {
    public static final Codec<EulerAngle> EULER_ANGLE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("pitch").forGetter(EulerAngle::getPitch),
            Codec.FLOAT.fieldOf("yaw").forGetter(EulerAngle::getYaw),
            Codec.FLOAT.fieldOf("roll").forGetter(EulerAngle::getRoll)
    ).apply(instance, EulerAngle::new));

    public static class MountedInventory {

        public record Slot(int index, ItemStack stack) {
            public static final Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("slot").forGetter(Slot::index),
                    ItemStack.CODEC.fieldOf("stack").forGetter(Slot::stack)
            ).apply(instance, Slot::new));

            public static final PacketCodec<RegistryByteBuf, Slot> PACKET_CODEC = PacketCodec.tuple(
                    PacketCodecs.VAR_INT, Slot::index,
                    ItemStack.PACKET_CODEC, Slot::stack,
                    Slot::new
            );
        }

        public static final Codec<LinkedHashSet<Slot>> CODEC =
                Codec.list(Slot.CODEC).xmap(LinkedHashSet::new, List::copyOf);

        public static final PacketCodec<RegistryByteBuf, LinkedHashSet<Slot>> PACKET_CODEC =
                Slot.PACKET_CODEC.collect(PacketCodecs.toCollection(LinkedHashSet::new));
    }

}
