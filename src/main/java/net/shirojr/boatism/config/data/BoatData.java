package net.shirojr.boatism.config.data;

@SuppressWarnings("ClassCanBeRecord")
public class BoatData {
    private final float lowHealthValue;

    public BoatData(float lowHealthValue) {
        this.lowHealthValue = lowHealthValue;
    }

    public float getLowHealthValue() {
        return lowHealthValue;
    }
}
