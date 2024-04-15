package net.shirojr.boatism.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;
import net.shirojr.boatism.util.data.EngineGuiTexture;

public class EngineControlScreen extends HandledScreen<EngineControlScreenHandler> {

    public EngineControlScreen(EngineControlScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        float heat = this.handler.getOverheat() / this.handler.getMaxOverheat();

        context.drawTexture(EngineGuiTexture.GUI_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        x = x + 16;
        y = y + 16;
        EngineGuiTexture.renderEngineParts(context, EngineGuiTexture.getAllPartsInOrder(), x, y, 1.0f, false);
        EngineGuiTexture.renderEngineParts(context, EngineGuiTexture.getAllPartsInOrder(), x, y, heat, true);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
