package net.shirojr.boatism.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;

public class EngineControlScreen extends HandledScreen<EngineControlScreenHandler> {
    public static final Identifier TEXTURE = new Identifier(Boatism.MODID, "textures/gui/engine_control.png");
    public static final EnginePartTexture TOP = new EnginePartTexture(206, 1, 29, 15);
    public static final EnginePartTexture MID = new EnginePartTexture(206, 17, 10, 8);
    public static final EnginePartTexture BOTTOM = new EnginePartTexture(206, 26, 6, 18);
    public static final EnginePartTexture TURBINE = new EnginePartTexture(206, 45, 14, 9);

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

        renderEngineOverlay(context, x + 16, y + 41, TURBINE);
        renderEngineOverlay(context, x + 17, y + 33, BOTTOM);
        renderEngineOverlay(context, x + 16, y + 29, MID);
        renderEngineOverlay(context, x + 19, y + 18, TOP);
    }

    public void renderEngineOverlay(DrawContext context, int x, int y, EnginePartTexture enginePart) {
        // TODO: implement maxHeat with delegates from handler to normalize heat
        float heat = this.handler.getOverheat();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, 0.0f);
        context.drawTexture(TEXTURE, x, y, enginePart.u, enginePart.v, enginePart.width, enginePart.height);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private record EnginePartTexture(int u, int v, int width, int height) {
    }
}
