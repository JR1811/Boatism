package net.shirojr.boatism.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseEngineItem extends Item {
    public BaseEngineItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        //I18n.translate(MinecraftClient.getInstance().options.sneakKey.getTranslationKey())
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_1"));
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_2"));
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_3"));
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_4"));
        tooltip.add(Text.translatable("tooltip.boatism.base_engine_5"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
