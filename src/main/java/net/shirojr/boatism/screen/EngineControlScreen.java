package net.shirojr.boatism.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;
import net.shirojr.boatism.util.gui.EngineGuiElement;
import net.shirojr.boatism.util.gui.FuelGuiElement;
import net.shirojr.boatism.util.gui.PowerLevelGuiElement;

public class EngineControlScreen extends HandledScreen<EngineControlScreenHandler> {
    private int tick = 0;
    private FuelGuiElement.FuelAnimation fuelSpriteState = FuelGuiElement.FuelAnimation.FIRST;

    public EngineControlScreen(EngineControlScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (!handler.isEngineRunning()) return;
        this.tick++;
        if (this.tick >= FuelGuiElement.TICKS_BETWEEN_SPRITE_CHANGE) {
            this.fuelSpriteState = FuelGuiElement.FuelAnimation.next(this.fuelSpriteState);
            this.tick = 0;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        if (this.client == null) return;
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        int engineSpriteX = x + 10;
        int engineSpriteY = y + 27;
        int fuelSpriteX = x + 55;
        int fuelSpriteY = y + 37;
        int powerLevelX = x + 55;
        int powerLevelY = y + 60;

        float heat = this.handler.getOverheat() / this.handler.getMaxOverheat();
        float fuel = this.handler.getFuel() / this.handler.getMaxFuel();

        context.drawTexture(EngineGuiElement.GUI_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        EngineGuiElement.renderEngineParts(context, EngineGuiElement.getAllPartsInOrder(),
                engineSpriteX, engineSpriteY, 1.0f, false);
        EngineGuiElement.renderEngineParts(context, EngineGuiElement.getAllPartsInOrder(),
                engineSpriteX, engineSpriteY, heat, true);
        FuelGuiElement.renderFuelGage(context, this.client.textRenderer, fuelSpriteX, fuelSpriteY, fuel, this.fuelSpriteState);
        PowerLevelGuiElement.renderElement(context, this.client.textRenderer, powerLevelX, powerLevelY, handler.getPowerLevel());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
