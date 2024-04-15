package net.shirojr.boatism.config.custom;

@SuppressWarnings("FieldMayBeFinal")
public class EngineHud {
    private boolean shouldDisplay;
    private int x, y;

    public EngineHud(boolean shouldDisplay, int x, int y) {
        this.shouldDisplay = shouldDisplay;
        this.x = x;
        this.y = y;
    }

    public boolean shouldDisplay() {
        return shouldDisplay;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
