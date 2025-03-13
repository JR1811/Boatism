package net.shirojr.boatism.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.shirojr.boatism.block.custom.FermentBlock;
import net.shirojr.boatism.init.BoatismBlocks;

import java.util.List;

public class BoatismModelGenerator extends FabricModelProvider {
    public BoatismModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        generator.excludeFromSimpleItemModelGeneration(BoatismBlocks.FERMENTER);
        MultipartBlockStateSupplier supplier = MultipartBlockStateSupplier.create(BoatismBlocks.FERMENTER);

        for (FermentBlock.Part part : FermentBlock.Part.values()) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                for (boolean open : List.of(true, false)) {
                    supplier.with(When.create()
                                    .set(FermentBlock.PART, part)
                                    .set(FermentBlock.FACING, direction)
                                    .set(FermentBlock.OPEN, open),
                            BlockStateVariant.create().put(VariantSettings.MODEL, Identifier.of("boatism", "block/fermenter"))
                    );
                }
            }
        }
        generator.blockStateCollector.accept(supplier);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {

    }
}
