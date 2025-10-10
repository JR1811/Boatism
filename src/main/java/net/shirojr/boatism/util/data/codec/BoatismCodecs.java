package net.shirojr.boatism.util.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
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

            // Packet serialization for 1.20.1
            public static Slot read(PacketByteBuf buf) {
                int slot = buf.readVarInt();
                ItemStack stack = buf.readItemStack();
                return new Slot(slot, stack);
            }

            public static void write(PacketByteBuf buf, Slot value) {
                buf.writeVarInt(value.index());
                buf.writeItemStack(value.stack());
            }
        }

        public static final Codec<LinkedHashSet<Slot>> CODEC =
                Codec.list(Slot.CODEC).xmap(LinkedHashSet::new, List::copyOf);

        // For network serialization
        public static LinkedHashSet<Slot> read(PacketByteBuf buf) {
            int size = buf.readVarInt();
            LinkedHashSet<Slot> set = new LinkedHashSet<>();
            for (int i = 0; i < size; i++) {
                set.add(Slot.read(buf));
            }
            return set;
        }

        public static void write(PacketByteBuf buf, LinkedHashSet<Slot> value) {
            buf.writeVarInt(value.size());
            for (Slot slot : value) {
                Slot.write(buf, slot);
            }
        }
    }
}