package net.shirojr.boatism.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;
import net.shirojr.boatism.util.geometry.ShapeUtil;
import net.shirojr.boatism.util.gui.EngineGuiElement;
import net.shirojr.boatism.util.gui.FuelGuiElement;
import net.shirojr.boatism.util.gui.PowerLevelGuiElement;

public class EngineControlScreen extends HandledScreen<EngineControlScreenHandler> {
    private int tick = 0;
    private boolean isHandlePressed = false;
    private int previousX = -1;
    private int draggedHorizontalDistance = 0;
    private ShapeUtil.Square handleSquare;
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
        int handlePosition = PowerLevelGuiElement.getPositionForPowerLevel(handler.getPowerLevel());

        context.drawTexture(EngineGuiElement.GUI_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        EngineGuiElement.renderEngineParts(context, EngineGuiElement.getAllPartsInOrder(),
                engineSpriteX, engineSpriteY, 1.0f, false);
        EngineGuiElement.renderEngineParts(context, EngineGuiElement.getAllPartsInOrder(),
                engineSpriteX, engineSpriteY, heat, true);
        FuelGuiElement.renderFuelGage(context, this.client.textRenderer, fuelSpriteX, fuelSpriteY, fuel, this.fuelSpriteState);

        handleSquare = new ShapeUtil.Square(new ShapeUtil.Position(powerLevelX + handlePosition, powerLevelY - 3), 5, 8);
        PowerLevelGuiElement.renderElement(context, this.client.textRenderer, powerLevelX, powerLevelY, handleSquare, this.isHandlePressed);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        if (!this.isHandlePressed) return;
        if (this.previousX != -1) {
            this.draggedHorizontalDistance += (int) mouseX - previousX;
            if (Math.abs(this.draggedHorizontalDistance) > PowerLevelGuiElement.getPositionForPowerLevel(1)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeDouble(Math.signum(this.draggedHorizontalDistance));
                ClientPlayNetworking.send(BoatismNetworkIdentifiers.POWER_LEVEL_CHANGE.getIdentifier(), buf);
                this.draggedHorizontalDistance = 0;
            }
        }
        this.previousX = (int) mouseX;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.handleSquare.isPositionInSquare(new ShapeUtil.Position((int) mouseX, (int) mouseY))) {
            if (!this.isHandlePressed) this.isHandlePressed = true;
            this.previousX = (int) mouseX;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isHandlePressed) {
            this.isHandlePressed = false;
            this.previousX = -1;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
