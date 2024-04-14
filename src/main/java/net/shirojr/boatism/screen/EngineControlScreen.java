package net.shirojr.boatism.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;
import net.shirojr.boatism.util.data.EnginePartTexture;

public class EngineControlScreen extends HandledScreen<EngineControlScreenHandler> {
    public static final Identifier TEXTURE = new Identifier(Boatism.MODID, "textures/gui/engine_control.png");

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

        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        x = x + 16;
        y = y + 16;
        renderEngineOverlay(context, x, y, EnginePartTexture.TURBINE);
        renderEngineOverlay(context, x, y, EnginePartTexture.BOTTOM);
        renderEngineOverlay(context, x, y, EnginePartTexture.MID);
        renderEngineOverlay(context, x, y, EnginePartTexture.TOP);
    }

    private void renderEngineOverlay(DrawContext context, int x, int y, EnginePartTexture part) {
        float heat = this.handler.getOverheat() / this.handler.getMaxOverheat();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, heat);
        context.drawTexture(TEXTURE, x + part.getXOffset(), y + part.getYOffset(),
                part.getU(true), part.getV(), part.getWidth(), part.getHeight());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
