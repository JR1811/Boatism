package net.shirojr.boatism.util.data;

/***
 *
 */
public enum EnginePartTexture {
    TOP (176, 1, 29, 15, 3, 2),
    MID (176, 17, 10, 8, 0, 13),
    BOTTOM (176, 26, 6, 18, 1, 17),
    TURBINE (176, 45, 14, 9, 0, 25);

    private final int u, v, width, height, xOffset, yOffset;

    EnginePartTexture(int u, int v, int width, int height, int xOffset, int yOffset) {
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
}
