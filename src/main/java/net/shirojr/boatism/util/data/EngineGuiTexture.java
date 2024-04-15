package net.shirojr.boatism.util.data;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public enum EngineGuiTexture {
    TOP(3, 176, 1, 29, 15, 3, 2),
    MID(2, 176, 17, 10, 8, 0, 13),
    BOTTOM(1, 176, 26, 6, 18, 1, 17),
    TURBINE(0, 176, 45, 14, 9, 0, 25);

    private final int index, u, v, width, height, xOffset, yOffset;
    public static final Identifier GUI_TEXTURE = new Identifier(Boatism.MODID, "textures/gui/engine_control.png");

    EngineGuiTexture(int index, int u, int v, int width, int height, int xOffset, int yOffset) {
        this.index = index;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public int getU(boolean useHeatedTexture) {
        if (!useHeatedTexture) return u;
        else return u + 30;
    }

    public int getV() {
        return v;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public static List<EngineGuiTexture> getAllPartsInOrder() {
        List<EngineGuiTexture> parts = new ArrayList<>(List.of(EngineGuiTexture.values()));
        Collections.reverse(parts);
        return parts;
    }

    public static void renderEngineParts(DrawContext context, List<EngineGuiTexture> parts, int x, int y, float alpha, boolean useHeatedTexture) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        for (EngineGuiTexture part : parts) {
            context.drawTexture(GUI_TEXTURE, x + part.getXOffset(), y + part.getYOffset(),
                    part.getU(useHeatedTexture), part.getV(), part.getWidth(), part.getHeight());
        }

        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
