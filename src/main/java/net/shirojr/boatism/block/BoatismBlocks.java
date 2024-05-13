package net.shirojr.boatism.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.block.custom.OilFluidBlock;
import net.shirojr.boatism.util.LoggerUtil;

public class BoatismBlocks {
    public static final OilFluidBlock OIL_FLUID_BLOCK =
            registerBlock("oil_fluid_block", false,
                    new OilFluidBlock(BoatismFluids.OIL.still(), FabricBlockSettings.copyOf(Blocks.WATER).mapColor(MapColor.BLACK)));


    @SuppressWarnings("SameParameterValue")
    private static <T extends Block> T registerBlock(String name, boolean registerBlockItem, T block) {
        Identifier identifier = new Identifier(Boatism.MODID, name);
        T registeredBlock = Registry.register(Registries.BLOCK, identifier, block);
        if (registerBlockItem) registerBlockItem(identifier, new BlockItem(block, new FabricItemSettings()));
        return registeredBlock;
    }

    private static <T extends Item> void registerBlockItem(Identifier identifier, T item) {
        Registry.register(Registries.ITEM, identifier, item);
    }

    public static void initialize() {
        LoggerUtil.devLogger("initialized blocks");
    }
}
