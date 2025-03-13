package net.shirojr.boatism.screen.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;

public class FuelGuiElement {
    public static final Identifier GUI_TEXTURE = new Identifier(Boatism.MODID, "textures/gui/engine_control.png");
    public static final int TICKS_BETWEEN_SPRITE_CHANGE = 10;

    private FuelGuiElement() {
    }

    public static void renderFuelGage(DrawContext context, TextRenderer textRenderer, int x, int y, float normalizedFill,
                                      FuelAnimation animationState) {
        int fuelMaxWidth = 52;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        context.drawTexture(GUI_TEXTURE, x, y, 176, 62, 54, 4);
        context.drawTexture(GUI_TEXTURE, x + 1, y + 1, 177, 67 + animationState.getShift(),
                Math.round(fuelMaxWidth * normalizedFill), 2);
        context.drawText(textRenderer, Text.translatable("gui.boatism.title.boat_engine.fuel"),
                x, y - 12, 0x404040, false);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public enum FuelAnimation {
        FIRST(0), SECOND(3), THIRD(6);
        private final int verticalAnimationShift;

        FuelAnimation(int shift) {
            this.verticalAnimationShift = shift;
        }

        public static FuelAnimation next(FuelAnimation oldEntry) {
            return switch (oldEntry) {
                case FIRST -> SECOND;
                case SECOND -> THIRD;
                case THIRD -> FIRST;
            };
        }

        public int getShift() {
            return this.verticalAnimationShift;
        }
    }
}
