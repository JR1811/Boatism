package net.shirojr.boatism.screen.handler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class EngineControlScreenHandler extends ScreenHandler {
    private final PlayerEntity player;

    protected EngineControlScreenHandler(int syncId, PlayerEntity player) {
        super(BoatismScreenHandlers.ENGINE_CONTROL_SCREEN_HANDLER, syncId);
        this.player = player;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }
}
