package net.shirojr.boatism.init;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.EulerAngle;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.util.data.BoatismCodecs;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface BoatismDataComponents {
    ComponentType<UUID> HOOKED_ENTITY = register("hooked_entity",
            optionalUuidBuilder -> optionalUuidBuilder
                    .codec(Uuids.CODEC)
                    .packetCodec(Uuids.PACKET_CODEC)
    );
    ComponentType<List<ItemStack>> MOUNTED_ITEMS = register("mounted_items",
            listBuilder -> listBuilder
                    .codec(ItemStack.CODEC.listOf())
                    .packetCodec(ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()))
    );
    ComponentType<Boolean> IS_RUNNING = register("is_running",
            booleanBuilder -> booleanBuilder
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOL)
    );
    ComponentType<Integer> POWER_OUTPUT = register("power_output",
            integerBuilder -> integerBuilder
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.INTEGER)
    );
    ComponentType<Float> OVERHEAT = register("overheat",
            floatBuilder -> floatBuilder
                    .codec(Codec.FLOAT)
                    .packetCodec(PacketCodecs.FLOAT)
    );
    ComponentType<EulerAngle> ROTATION = register("rotation",
            eulerAngleBuilder -> eulerAngleBuilder
                    .codec(BoatismCodecs.EULER_ANGLE)
                    .packetCodec(EulerAngle.PACKET_CODEC)
    );
    ComponentType<Boolean> IS_SUBMERGED = register("is_submerged",
            booleanBuilder -> booleanBuilder
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOL)
    );
    ComponentType<Long> FUEL = register("fuel",
            longBuilder -> longBuilder
                    .codec(Codec.LONG)
                    .packetCodec(PacketCodecs.VAR_LONG)
    );
    ComponentType<Boolean> IS_LOCKED = register("is_locked",
            booleanBuilder -> booleanBuilder
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOL)
    );
    ComponentType<ItemStack> DISPLAYED_ITEM = register("displayed_item",
            itemStackBuilder -> itemStackBuilder
                    .codec(ItemStack.CODEC)
                    .packetCodec(ItemStack.PACKET_CODEC)
    );
    ComponentType<ItemStack> ORIGINAL_ITEM = register("original_item",
            itemStackBuilder -> itemStackBuilder
                    .codec(ItemStack.CODEC)
                    .packetCodec(ItemStack.PACKET_CODEC)
    );

    @SuppressWarnings("SameParameterValue")
    private static <T> ComponentType<T> register(String name, Consumer<ComponentType.Builder<T>> componentTypeConsumer) {
        ComponentType.Builder<T> builder = ComponentType.builder();
        componentTypeConsumer.accept(builder);
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Boatism.getId(name), builder.build());
    }

    static void initialize() {
        // static initialisation
    }
}
