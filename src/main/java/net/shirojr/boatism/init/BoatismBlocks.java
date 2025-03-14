package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.block.custom.FermentBlock;
import net.shirojr.boatism.block.custom.OilFluidBlock;
import net.shirojr.boatism.util.LoggerUtil;

public interface BoatismBlocks {
    OilFluidBlock OIL_FLUID_BLOCK = registerBlock("oil_fluid_block", false,
            new OilFluidBlock(BoatismFluids.OIL.still(), FabricBlockSettings.copyOf(Blocks.WATER).mapColor(MapColor.BLACK)));
    FermentBlock FERMENTER = registerBlock("fermenter", false,
            new FermentBlock(AbstractBlock.Settings.create().nonOpaque().mapColor(MapColor.TEAL)));


    @SuppressWarnings("SameParameterValue")
    private static <T extends Block> T registerBlock(String name, boolean registerBlockItem, T block) {
        Identifier identifier = Boatism.getId(name);
        T registeredBlock = Registry.register(Registries.BLOCK, identifier, block);
        if (registerBlockItem) registerBlockItem(identifier, new BlockItem(block, new Item.Settings()));
        return registeredBlock;
    }

    private static <T extends Item> void registerBlockItem(Identifier identifier, T item) {
        T registeredEntry = Registry.register(Registries.ITEM, identifier, item);
        BoatismItems.ALL_ITEMS.add(new ItemStack(registeredEntry));
    }

    static void initialize() {
        LoggerUtil.devLogger("initialized blocks");
    }
}
