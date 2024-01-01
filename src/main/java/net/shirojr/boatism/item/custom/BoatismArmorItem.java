package net.shirojr.boatism.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.shirojr.boatism.api.BoatEngineComponent;

public class BoatismArmorItem extends ArmorItem implements BoatEngineComponent {
    public BoatismArmorItem(ArmorMaterial material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public TypedActionResult<ItemStack> equipAndSwap(Item item, World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user instanceof PlayerEntity) return TypedActionResult.pass(itemStack);
        return super.equipAndSwap(item, world, user, hand);
    }
}
