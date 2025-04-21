package net.shirojr.boatism.item.custom.engine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.item.util.EnginePlacer;

import java.util.List;

public class BaseEngineItem extends Item implements EnginePlacer {
    public BaseEngineItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        //I18n.translate(MinecraftClient.getInstance().options.sneakKey.getTranslationKey())
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_1"));
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_2"));
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_3"));
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_4"));
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_5"));
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public BoatEngineEntity getEngineInstance(World world, BoatEntity boat) {
        return new BoatEngineEntity(world, boat);
    }
}
