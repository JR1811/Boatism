package net.shirojr.boatism.screen.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.screen.geometry.ShapeUtil;

public class PowerLevelGuiElement {
    public static final Identifier GUI_TEXTURE = new Identifier(Boatism.MODID, "textures/gui/engine_control.png");
    public static final int WIDTH = 53;

    private PowerLevelGuiElement() {
    }

    public static void renderElement(DrawContext context, TextRenderer textRenderer, int x, int y, ShapeUtil.Square handle, boolean isHandlePressed) {
        int handleShift = isHandlePressed ? 6 : 0;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        context.drawTexture(GUI_TEXTURE, x, y, 177, 87, WIDTH, 2);
        context.drawTexture(GUI_TEXTURE, handle.getSquareStart().x(), handle.getSquareStart().y(),
                177 + handleShift, 90, handle.getWidth(), handle.getHeight());
        context.drawText(textRenderer, Text.translatable("gui.boatism.title.boat_engine.power_level"),
                x, y - 12, 0x404040, false);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public static int getPositionForPowerLevel(int powerLevel) {
        float normalizedLevel = (powerLevel) * 0.1f;
        return Math.round(PowerLevelGuiElement.WIDTH * normalizedLevel);
    }

    public static ShapeUtil.Square getHandlePositions(int x, int y) {
        return new ShapeUtil.Square(new ShapeUtil.Position(x, y), new ShapeUtil.Position(x + 5, y + 8));
    }

}
