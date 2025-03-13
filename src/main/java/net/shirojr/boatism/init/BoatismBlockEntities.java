package net.shirojr.boatism.init;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.block.entity.custom.FermentBlockEntity;

public interface BoatismBlockEntities {
    BlockEntityType<FermentBlockEntity> FERMENTER = register("fermenter", FermentBlockEntity::new, BoatismBlocks.FERMENTER);


    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       BlockEntityType.BlockEntityFactory<? extends T> entityFactory,
                                                                       Block... blocks) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Boatism.getId(name),
                BlockEntityType.Builder.<T>create(entityFactory, blocks).build());
    }

    static void initialize() {
        // static initialisation
    }
}
